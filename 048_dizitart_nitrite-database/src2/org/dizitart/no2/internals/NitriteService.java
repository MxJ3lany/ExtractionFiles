/*
 *
 * Copyright 2017-2018 Nitrite author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dizitart.no2.internals;

import org.dizitart.no2.*;
import org.dizitart.no2.event.ChangeInfo;
import org.dizitart.no2.event.ChangeListener;
import org.dizitart.no2.event.EventBus;
import org.dizitart.no2.fulltext.EnglishTextTokenizer;
import org.dizitart.no2.fulltext.TextIndexingService;
import org.dizitart.no2.fulltext.TextTokenizer;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.store.NitriteMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.dizitart.no2.exceptions.ErrorCodes.*;
import static org.dizitart.no2.exceptions.ErrorMessage.errorMessage;
import static org.dizitart.no2.util.ValidationUtils.notNull;

/**
 * A service class for Nitrite database operations.
 *
 * @since 1.0
 * @author Anindya Chatterjee
 */
public class NitriteService {
    private NitriteContext nitriteContext;
    private final NitriteMap<NitriteId, Document> mapStore;
    private IndexingService indexingService;
    private DataService dataService;
    private SearchService searchService;
    private IndexedSearchService indexedSearchService;
    private IndexMetaService indexMetaService;
    private EventBus<ChangeInfo, ChangeListener> eventBus;

    /**
     * Instantiates a new Nitrite service.
     *
     * @param mapStore       the map store
     * @param nitriteContext the nitrite context
     */
    NitriteService(NitriteMap<NitriteId, Document> mapStore,
                   NitriteContext nitriteContext,
                   EventBus<ChangeInfo, ChangeListener> eventBus) {
        this.mapStore = mapStore;
        this.nitriteContext = nitriteContext;
        this.eventBus = eventBus;
        init();
    }

    /**
     * Specifies if an indexing operation is currently running.
     *
     * @param field the field
     * @return `true` if operation is still running; `false` otherwise.
     */
    public boolean isIndexing(String field) {
        notNull(field, errorMessage("field can not be null", VE_IS_INDEXING_NULL_FIELD));
        return indexingService.isIndexing(field);
    }

    /**
     * Specifies if a value is indexed.
     *
     * @param field the field
     * @return `true` if indexed; `false` otherwise.
     */
    public boolean hasIndex(String field) {
        notNull(field, errorMessage("field can not be null", VE_HAS_INDEX_NULL_FIELD));
        return indexMetaService.hasIndex(field);
    }

    /**
     * Finds with equal filer using index.
     *
     * @param field the field
     * @param value the value
     * @return the result set.
     */
    public Set<NitriteId> findEqualWithIndex(String field, Object value) {
        notNull(field, errorMessage("field can not be null", VE_FIND_EQUAL_INDEX_NULL_FIELD));
        if (value == null) return new HashSet<>();
        return indexedSearchService.findEqual(field, value);
    }

    /**
     * Finds with greater than filer using index.
     *
     * @param field the field
     * @param value the value
     * @return the result set
     */
    public Set<NitriteId> findGreaterThanWithIndex(String field, Comparable value) {
        notNull(field, errorMessage("field can not be null", VE_FIND_GT_INDEX_NULL_FIELD));
        notNull(value, errorMessage("value can not be null", VE_FIND_GT_INDEX_NULL_VALUE));
        return indexedSearchService.findGreaterThan(field, value);
    }

    /**
     * Finds with greater and equal filer using index.
     *
     * @param field the field
     * @param value the value
     * @return the result set
     */
    public Set<NitriteId> findGreaterEqualWithIndex(String field, Comparable value) {
        notNull(field, errorMessage("field can not be null", VE_FIND_GTE_INDEX_NULL_FIELD));
        notNull(value, errorMessage("value can not be null", VE_FIND_GTE_INDEX_NULL_VALUE));
        return indexedSearchService.findGreaterEqual(field, value);
    }

    /**
     * Finds with lesser filer using index.
     *
     * @param field the field
     * @param value the value
     * @return the result set
     */
    public Set<NitriteId> findLesserThanWithIndex(String field, Comparable value) {
        notNull(field, errorMessage("field can not be null", VE_FIND_LT_INDEX_NULL_FIELD));
        notNull(value, errorMessage("value can not be null", VE_FIND_LT_INDEX_NULL_VALUE));
        return indexedSearchService.findLesserThan(field, value);
    }

    /**
     * Finds with lesser equal filer using index.
     *
     * @param field the field
     * @param value the value
     * @return the result set
     */
    public Set<NitriteId> findLesserEqualWithIndex(String field, Comparable value) {
        notNull(field, errorMessage("field can not be null", VE_FIND_LTE_INDEX_NULL_FIELD));
        notNull(value, errorMessage("value can not be null", VE_FIND_LTE_INDEX_NULL_VALUE));
        return indexedSearchService.findLesserEqual(field, value);
    }

    /**
     * Finds with in filer using index.
     *
     * @param field  the value
     * @param values the values
     * @return the result set
     */
    public Set<NitriteId> findInWithIndex(String field, List<Object> values) {
        notNull(field, errorMessage("field can not be null", VE_FIND_IN_INDEX_NULL_FIELD));
        notNull(values, errorMessage("values can not be null", VE_FIND_IN_INDEX_NULL_VALUE));
        return indexedSearchService.findIn(field, values);
    }

    /**
     * Finds with text filer using full-text index.
     *
     * @param field the value
     * @param value the value
     * @return the result set
     */
    public Set<NitriteId> findTextWithIndex(String field, String value) {
        notNull(field, errorMessage("field can not be null", VE_FIND_TEXT_INDEX_NULL_FIELD));
        notNull(value, errorMessage("value can not be null", VE_FIND_TEXT_INDEX_NULL_VALUE));
        return indexedSearchService.findText(field, value);
    }

    /**
     * Gets the {@link NitriteMapper} implementation.
     *
     * @return the nitrite mapper
     */
    public NitriteMapper getNitriteMapper() {
        return nitriteContext.getNitriteMapper();
    }


    /**
     * Gets the nitrite context.
     *
     * @return the nitrite context.
     */
    public NitriteContext getNitriteContext() {
        return nitriteContext;
    }

    /**
     * Creates an index.
     *
     * @param field     the value
     * @param indexType the index type
     * @param async     asynchronous operation if set to `true`
     */
    void createIndex(String field, IndexType indexType, boolean async) {
        notNull(field, errorMessage("field can not be null", VE_CREATE_INDEX_NULL_FIELD));
        notNull(indexType, errorMessage("indexType can not be null", VE_CREATE_INDEX_NULL_INDEX_TYPE));
        indexingService.createIndex(field, indexType, async);
    }

    /**
     * Rebuilds an index.
     *
     * @param index   the index
     * @param isAsync asynchronous operation if set to `true`
     */
    void rebuildIndex(Index index, boolean isAsync) {
        notNull(index, errorMessage("index can not be null", VE_REBUILD_INDEX_NULL_INDEX));
        indexingService.rebuildIndex(index, isAsync);
    }

    /**
     * Finds the index information of a value.
     *
     * @param field the value
     * @return the index information.
     */
    Index findIndex(String field) {
        notNull(field, errorMessage("field can not be null", VE_FIND_INDEX_NULL_INDEX));
        return indexMetaService.findIndex(field);
    }

    /**
     * Drops the index of a value.
     *
     * @param field the value
     */
    void dropIndex(String field) {
        notNull(field, errorMessage("field can not be null", VE_DROP_INDEX_NULL_FIELD));
        indexingService.dropIndex(field);
    }

    /**
     * Drops all indices.
     */
    void dropAllIndices() {
        indexingService.dropAllIndices();
    }

    /**
     * Gets indices information of all indexed fields.
     *
     * @return the collection of index information.
     */
    Collection<Index> listIndexes() {
        return indexingService.listIndexes();
    }

    /**
     * Inserts documents in the database.
     *
     * @param document  the document to insert
     * @param documents other documents to insert
     * @return the write result
     */
    WriteResultImpl insert(Document document, Document... documents) {
        notNull(document, errorMessage("document can not be null", VE_INSERT_NULL_DOCUMENT));

        int length = documents == null ? 0 : documents.length;

        if (length > 0) {
            Document[] array = new Document[length + 1];
            array[0] = document;
            System.arraycopy(documents, 0, array, 1, length);
            return dataService.insert(array);
        } else {
            return dataService.insert(document);
        }
    }

    /**
     * Inserts documents in the database.
     *
     * @param documents the documents to insert
     * @return the write result
     */
    WriteResult insert(Document[] documents) {
        notNull(documents, errorMessage("documents can not be null", VE_INSERT_NULL_DOCUMENT_ARRAY));
        return dataService.insert(documents);
    }

    /**
     * Queries the database.
     *
     * @param filter the filter
     * @return the result set
     */
    Cursor find(Filter filter) {
        return searchService.find(filter);
    }

    /**
     * Returns ids of all records stored in the database.
     *
     * @return the result set
     */
    Cursor find() {
        return searchService.find();
    }

    /**
     * Queries the database.
     *
     * @param findOptions the find options
     * @return the result set
     */
    Cursor find(FindOptions findOptions) {
        notNull(findOptions, errorMessage("findOptions can not be null", VE_FIND_NULL_FIND_OPTIONS));
        return searchService.find(findOptions);
    }

    /**
     * Queries the database.
     *
     * @param filter      the filter
     * @param findOptions the find options
     * @return the result set
     */
    Cursor find(Filter filter, FindOptions findOptions) {
        notNull(findOptions, errorMessage("findOptions can not be null", VE_FIND_FILTERED_NULL_FIND_OPTIONS));
        return searchService.find(filter, findOptions);
    }

    /**
     * Gets a document by its id.
     *
     * @param nitriteId the nitrite id
     * @return the document associated with the id; `null` otherwise.
     */
    Document getById(NitriteId nitriteId) {
        notNull(nitriteId, errorMessage("nitriteId can not be null", VE_GET_BY_ID_NULL_ID));
        return dataService.getById(nitriteId);
    }

    /**
     * Updates a document in the database.
     *
     * @param filter        the filter
     * @param update        the update
     * @param updateOptions the update options
     * @return the write result
     */
    WriteResultImpl update(Filter filter, Document update, UpdateOptions updateOptions) {
        notNull(update, errorMessage("update document can not be null", VE_UPDATE_NULL_DOCUMENT));
        notNull(updateOptions, errorMessage("updateOptions can not be null", VE_UPDATE_NULL_UPDATE_OPTIONS));
        return dataService.update(filter, update, updateOptions);
    }

    /**
     * Removes documents from the database.
     *
     * @param filter        the filter
     * @param removeOptions the remove options
     * @return the write result
     */
    WriteResultImpl remove(Filter filter, RemoveOptions removeOptions) {
        return dataService.remove(filter, removeOptions);
    }

    /**
     * Drops a nitrite collection from the store.
     */
    void dropCollection() {
        indexingService.dropAllIndices();
        nitriteContext.dropCollection(mapStore.getName());
        mapStore.getStore().removeMap(mapStore);
    }

    /**
     * Gets text indexing service.
     *
     * @return the text indexing service
     */
    private TextIndexingService getTextIndexingService() {
        TextIndexingService textIndexingService = nitriteContext.getTextIndexingService();
        TextTokenizer textTokenizer = getTextTokenizer();

        if (textIndexingService == null) {
            textIndexingService = new NitriteTextIndexingService(textTokenizer, indexMetaService);
        }
        return textIndexingService;
    }

    /**
     * Gets text tokenizer.
     *
     * @return the text tokenizer
     */
    private TextTokenizer getTextTokenizer() {
        TextTokenizer textTokenizer = nitriteContext.getTextTokenizer();
        if (textTokenizer == null) {
            textTokenizer = new EnglishTextTokenizer();
        }
        return textTokenizer;
    }

    private void init() {
        this.indexMetaService = new IndexMetaService(mapStore);
        TextIndexingService textIndexingService = getTextIndexingService();

        this.indexingService = new IndexingService(indexMetaService, textIndexingService, nitriteContext);
        this.indexedSearchService = new IndexedSearchService(indexMetaService, textIndexingService);
        this.searchService = new SearchService(this, mapStore);
        this.dataService = new DataService(indexingService, searchService, mapStore, eventBus);
    }
}
