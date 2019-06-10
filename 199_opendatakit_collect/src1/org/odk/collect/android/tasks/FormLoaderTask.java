/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.tasks;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;

import org.apache.commons.io.IOUtils;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.instance.InstanceInitializationFactory;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.instance.utils.DefaultAnswerResolver;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.RootTranslator;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.util.XFormUtils;
import org.javarosa.xpath.XPathTypeMismatchException;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.ItemsetDbAdapter;
import org.odk.collect.android.external.ExternalAnswerResolver;
import org.odk.collect.android.external.ExternalDataHandler;
import org.odk.collect.android.external.ExternalDataManager;
import org.odk.collect.android.external.ExternalDataManagerImpl;
import org.odk.collect.android.external.ExternalDataReader;
import org.odk.collect.android.external.ExternalDataReaderImpl;
import org.odk.collect.android.external.handler.ExternalDataHandlerPull;
import org.odk.collect.android.listeners.FormLoaderListener;
import org.odk.collect.android.logic.FileReferenceFactory;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FormDefCache;
import org.odk.collect.android.utilities.ZipUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;
import timber.log.Timber;

/**
 * Background task for loading a form.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class FormLoaderTask extends AsyncTask<String, String, FormLoaderTask.FECWrapper> {
    private static final String ITEMSETS_CSV = "itemsets.csv";

    private FormLoaderListener stateListener;
    private String errorMsg;
    private String instancePath;
    private final String xpath;
    private final String waitingXPath;
    private boolean pendingActivityResult;
    private int requestCode;
    private int resultCode;
    private Intent intent;
    private ExternalDataManager externalDataManager;
    private FormDef formDef;

    protected static class FECWrapper {
        FormController controller;
        boolean usedSavepoint;

        protected FECWrapper(FormController controller, boolean usedSavepoint) {
            this.controller = controller;
            this.usedSavepoint = usedSavepoint;
        }

        protected FormController getController() {
            return controller;
        }

        protected boolean hasUsedSavepoint() {
            return usedSavepoint;
        }

        protected void free() {
            controller = null;
        }
    }

    FECWrapper data;

    public FormLoaderTask(String instancePath, String xpath, String waitingXPath) {
        this.instancePath = instancePath;
        this.xpath = xpath;
        this.waitingXPath = waitingXPath;
    }

    /**
     * Initialize {@link FormEntryController} with {@link FormDef} from binary or
     * from XML. If given an instance, it will be used to fill the {@link FormDef}.
     */
    @Override
    protected FECWrapper doInBackground(String... path) {
        errorMsg = null;

        final String formPath = path[0];
        if (formPath == null) {
            Timber.e("formPath is null");
            errorMsg = "formPath is null, please post on the forum with a description of what you were doing when this happened.";
            return null;
        }

        final File formXml = new File(formPath);
        final File formMediaDir = FileUtils.getFormMediaDir(formXml);

        final ReferenceManager referenceManager = ReferenceManager.instance();

        // Remove previous forms
        referenceManager.clearSession();

        // This should get moved to the Application Class
        if (referenceManager.getFactories().length == 0) {
            // this is /sdcard/odk
            referenceManager.addReferenceFactory(new FileReferenceFactory(Collect.ODK_ROOT));
        }

        addSessionRootTranslators(formMediaDir.getName(), referenceManager,
                "images", "image", "audio", "video", "file");

        FormDef formDef = null;
        try {
            formDef = createFormDefFromCacheOrXml(formPath, formXml);
        } catch (StackOverflowError e) {
            Timber.e(e);
            errorMsg = Collect.getInstance().getString(R.string.too_complex_form);
        }

        if (errorMsg != null || formDef == null) {
            return null;
        }

        externalDataManager = new ExternalDataManagerImpl(formMediaDir);

        // add external data function handlers
        ExternalDataHandler externalDataHandlerPull = new ExternalDataHandlerPull(
                externalDataManager);
        formDef.getEvaluationContext().addFunctionHandler(externalDataHandlerPull);

        try {
            loadExternalData(formMediaDir);
        } catch (Exception e) {
            Timber.e(e, "Exception thrown while loading external data");
            errorMsg = e.getMessage();
            return null;
        }

        if (isCancelled()) {
            // that means that the user has cancelled, so no need to go further
            return null;
        }

        // create FormEntryController from formdef
        final FormEntryModel fem = new FormEntryModel(formDef);
        final FormEntryController fec = new FormEntryController(fem);

        boolean usedSavepoint = false;

        try {
            Timber.i("Initializing form.");
            final long start = System.currentTimeMillis();
            usedSavepoint = initializeForm(formDef, fec);
            Timber.i("Form initialized in %.3f seconds.", (System.currentTimeMillis() - start) / 1000F);
        } catch (IOException | RuntimeException e) {
            Timber.e(e);
            if (e.getCause() instanceof XPathTypeMismatchException) {
                // this is a case of
                // https://bitbucket.org/m
                // .sundt/javarosa/commits/e5d344783e7968877402bcee11828fa55fac69de
                // the data are imported, the survey will be unusable
                // but we should give the option to the user to edit the form
                // otherwise the survey will be TOTALLY inaccessible.
                Timber.w("We have a syntactically correct instance, but the data threw an "
                                + "exception inside JR. We should allow editing.");
            } else {
                errorMsg = e.getMessage();
                return null;
            }
        }

        processItemSets(formMediaDir);

        final FormController fc = new FormController(formMediaDir, fec, instancePath == null ? null
                : new File(instancePath));
        if (xpath != null) {
            // we are resuming after having terminated -- set index to this
            // position...
            FormIndex idx = fc.getIndexFromXPath(xpath);
            fc.jumpToIndex(idx);
        }
        if (waitingXPath != null) {
            FormIndex idx = fc.getIndexFromXPath(waitingXPath);
            fc.setIndexWaitingForData(idx);
        }
        data = new FECWrapper(fc, usedSavepoint);
        return data;
    }

    private void addSessionRootTranslators(String formMediaDir, ReferenceManager referenceManager, String... hostStrings) {
        // Set jr://... to point to /sdcard/odk/forms/formBasename-media/
        final String translatedPrefix = String.format("jr://file/forms/" + formMediaDir + "/");
        for (String t : hostStrings) {
            referenceManager.addSessionRootTranslator(new RootTranslator(String.format("jr://%s/", t), translatedPrefix));
        }
    }

    private FormDef createFormDefFromCacheOrXml(String formPath, File formXml) {
        publishProgress(
                Collect.getInstance().getString(R.string.survey_loading_reading_form_message));

        final FormDef formDefFromCache = FormDefCache.readCache(formXml);
        if (formDefFromCache != null) {
            return formDefFromCache;
        }

        FileInputStream fis = null;
        // no binary, read from xml
        try {
            Timber.i("Attempting to load from: %s", formXml.getAbsolutePath());
            final long start = System.currentTimeMillis();
            fis = new FileInputStream(formXml);
            String lastSavedSrc = FileUtils.getOrCreateLastSavedSrc(formXml);
            FormDef formDefFromXml = XFormUtils.getFormFromInputStream(fis, lastSavedSrc);
            if (formDefFromXml == null) {
                errorMsg = "Error reading XForm file";
            } else {
                Timber.i("Loaded in %.3f seconds.",
                        (System.currentTimeMillis() - start) / 1000F);
                formDef = formDefFromXml;

                FormDefCache.writeCache(formDef, formPath);

                return formDefFromXml;
            }
        } catch (Exception e) {
            Timber.e(e);
            errorMsg = e.getMessage();
        } finally {
            IOUtils.closeQuietly(fis);
        }
        return null;
    }

    private void processItemSets(File formMediaDir) {
        // for itemsets.csv, we only check to see if the itemset file has been
        // updated
        final File csv = new File(formMediaDir.getAbsolutePath() + "/" + ITEMSETS_CSV);
        String csvmd5 = null;
        if (csv.exists()) {
            csvmd5 = FileUtils.getMd5Hash(csv);
            boolean readFile = false;
            final ItemsetDbAdapter ida = new ItemsetDbAdapter();
            ida.open();
            // get the database entry (if exists) for this itemsets.csv, based
            // on the path
            final Cursor c = ida.getItemsets(csv.getAbsolutePath());
            if (c != null) {
                if (c.getCount() == 1) {
                    c.moveToFirst(); // should be only one, ever, if any
                    final String oldmd5 = c.getString(c.getColumnIndex("hash"));
                    if (oldmd5.equals(csvmd5)) {
                        // they're equal, do nothing
                    } else {
                        // the csv has been updated, delete the old entries
                        ida.dropTable(ItemsetDbAdapter.getMd5FromString(csv.getAbsolutePath()),
                                csv.getAbsolutePath());
                        // and read the new
                        readFile = true;
                    }
                } else {
                    // new csv, add it
                    readFile = true;
                }
                c.close();
            }
            ida.close();
            if (readFile) {
                readCSV(csv, csvmd5, ItemsetDbAdapter.getMd5FromString(csv.getAbsolutePath()));
            }
        }
    }

    private boolean initializeForm(FormDef formDef, FormEntryController fec) throws IOException {
        final InstanceInitializationFactory instanceInit = new InstanceInitializationFactory();
        boolean usedSavepoint = false;

        if (instancePath != null) {
            File instanceXml = new File(instancePath);

            // Use the savepoint file only if it's newer than the last manual save
            final File savepointFile = SaveToDiskTask.getSavepointFile(instanceXml.getName());
            if (savepointFile.exists()
                    && savepointFile.lastModified() > instanceXml.lastModified()) {
                usedSavepoint = true;
                instanceXml = savepointFile;
                Timber.w("Loading instance from savepoint file: %s",
                        savepointFile.getAbsolutePath());
            }

            if (instanceXml.exists()) {
                // This order is important. Import data, then initialize.
                try {
                    Timber.i("Importing data");
                    publishProgress(Collect.getInstance().getString(R.string.survey_loading_reading_data_message));
                    importData(instanceXml, fec);
                    formDef.initialize(false, instanceInit);
                } catch (IOException | RuntimeException e) {
                    Timber.e(e);

                    // Skip a savepoint file that is corrupted or 0-sized
                    if (usedSavepoint && !(e.getCause() instanceof XPathTypeMismatchException)) {
                        usedSavepoint = false;
                        instancePath = null;
                        formDef.initialize(true, instanceInit);
                    } else {
                        // The saved instance is corrupted.
                        throw e;
                    }
                }
            } else {
                formDef.initialize(true, instanceInit);
            }
        } else {
            formDef.initialize(true, instanceInit);
        }
        return usedSavepoint;
    }

    @SuppressWarnings("unchecked")
    private void loadExternalData(File mediaFolder) {
        // SCTO-594
        File[] zipFiles = mediaFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().toLowerCase(Locale.US).endsWith(".zip");
            }
        });

        if (zipFiles != null) {
            ZipUtils.unzip(zipFiles);
            for (File zipFile : zipFiles) {
                boolean deleted = zipFile.delete();
                if (!deleted) {
                    Timber.w("Cannot delete %s. It will be re-unzipped next time. :(", zipFile.toString());
                }
            }
        }

        File[] csvFiles = mediaFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String lowerCaseName = file.getName().toLowerCase(Locale.US);
                return lowerCaseName.endsWith(".csv") && !lowerCaseName.equalsIgnoreCase(
                        ITEMSETS_CSV);
            }
        });

        Map<String, File> externalDataMap = new HashMap<String, File>();

        if (csvFiles != null) {

            for (File csvFile : csvFiles) {
                String dataSetName = csvFile.getName().substring(0,
                        csvFile.getName().lastIndexOf("."));
                externalDataMap.put(dataSetName, csvFile);
            }

            if (!externalDataMap.isEmpty()) {

                publishProgress(Collect.getInstance()
                        .getString(R.string.survey_loading_reading_csv_message));

                ExternalDataReader externalDataReader = new ExternalDataReaderImpl(this);
                externalDataReader.doImport(externalDataMap);
            }
        }
    }

    public void publishExternalDataLoadingProgress(String message) {
        publishProgress(message);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        synchronized (this) {
            if (stateListener != null && values != null) {
                if (values.length == 1) {
                    stateListener.onProgressStep(values[0]);
                }
            }
        }
    }

    public static void importData(File instanceFile, FormEntryController fec) throws IOException, RuntimeException {
        // convert files into a byte array
        byte[] fileBytes = org.apache.commons.io.FileUtils.readFileToByteArray(instanceFile);

        // get the root of the saved and template instances
        TreeElement savedRoot = XFormParser.restoreDataModel(fileBytes, null).getRoot();
        TreeElement templateRoot = fec.getModel().getForm().getInstance().getRoot().deepCopy(true);

        // weak check for matching forms
        if (!savedRoot.getName().equals(templateRoot.getName()) || savedRoot.getMult() != 0) {
            Timber.e("Saved form instance does not match template form definition");
            return;
        }

        // populate the data model
        TreeReference tr = TreeReference.rootRef();
        tr.add(templateRoot.getName(), TreeReference.INDEX_UNBOUND);

        // Here we set the Collect's implementation of the IAnswerResolver.
        // We set it back to the default after select choices have been populated.
        XFormParser.setAnswerResolver(new ExternalAnswerResolver());
        templateRoot.populate(savedRoot, fec.getModel().getForm());
        XFormParser.setAnswerResolver(new DefaultAnswerResolver());

        // populated model to current form
        fec.getModel().getForm().getInstance().setRoot(templateRoot);

        // fix any language issues
        // :
        // http://bitbucket.org/javarosa/main/issue/5/itext-n-appearing-in-restored-instances
        if (fec.getModel().getLanguages() != null) {
            fec.getModel().getForm()
                    .localeChanged(fec.getModel().getLanguage(),
                            fec.getModel().getForm().getLocalizer());
        }
        Timber.i("Done importing data");
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        if (externalDataManager != null) {
            externalDataManager.close();
        }
    }

    @Override
    protected void onPostExecute(FECWrapper wrapper) {
        synchronized (this) {
            try {
                if (stateListener != null) {
                    if (wrapper == null) {
                        stateListener.loadingError(errorMsg);
                    } else {
                        stateListener.loadingComplete(this, formDef);
                    }
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    public void setFormLoaderListener(FormLoaderListener sl) {
        synchronized (this) {
            stateListener = sl;
        }
    }

    public FormController getFormController() {
        return (data != null) ? data.getController() : null;
    }

    public ExternalDataManager getExternalDataManager() {
        return externalDataManager;
    }

    public boolean hasUsedSavepoint() {
        return (data != null) && data.hasUsedSavepoint();
    }

    public void destroy() {
        if (data != null) {
            data.free();
            data = null;
        }
    }

    public boolean hasPendingActivityResult() {
        return pendingActivityResult;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public int getResultCode() {
        return resultCode;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setActivityResult(int requestCode, int resultCode, Intent intent) {
        this.pendingActivityResult = true;
        this.requestCode = requestCode;
        this.resultCode = resultCode;
        this.intent = intent;
    }

    private void readCSV(File csv, String formHash, String pathHash) {

        CSVReader reader;
        ItemsetDbAdapter ida = new ItemsetDbAdapter();
        ida.open();
        boolean withinTransaction = false;

        try {
            reader = new CSVReader(new FileReader(csv));

            String[] nextLine;
            String[] columnHeaders = null;
            int lineNumber = 0;
            while ((nextLine = reader.readNext()) != null) {
                lineNumber++;
                if (lineNumber == 1) {
                    // first line of csv is column headers
                    columnHeaders = nextLine;
                    ida.createTable(formHash, pathHash, columnHeaders,
                            csv.getAbsolutePath());
                    continue;
                }
                // add the rest of the lines to the specified database
                // nextLine[] is an array of values from the line
                // System.out.println(nextLine[4] + "etc...");
                if (lineNumber == 2) {
                    // start a transaction for the inserts
                    withinTransaction = true;
                    ida.beginTransaction();
                }
                ida.addRow(pathHash, columnHeaders, nextLine);

            }
        } catch (IOException e) {
            Timber.e(e, "Exception thrown while reading csv file");
        } finally {
            if (withinTransaction) {
                ida.commit();
            }
            ida.close();
        }
    }

    public FormDef getFormDef() {
        return formDef;
    }
}
