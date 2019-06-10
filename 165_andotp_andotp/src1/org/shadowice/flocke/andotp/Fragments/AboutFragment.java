package org.shadowice.flocke.andotp.Fragments;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.ColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.Settings;
import org.shadowice.flocke.andotp.Utilities.Tools;

public class AboutFragment extends Fragment {
    private static final String GITHUB_URI = "https://github.com/andOTP/andOTP";
    private static final String CHANGELOG_URI = GITHUB_URI + "/blob/master/CHANGELOG.md";
    private static final String MIT_URI = GITHUB_URI + "/blob/master/LICENSE.txt";

    private static final String AUTHOR1_GITHUB = "https://github.com/flocke";
    private static final String AUTHOR1_EXTRA = "https://paypal.me/flocke000";

    private static final String AUTHOR2_GITHUB = "https://github.com/richyhbm";
    private static final String AUTHOR2_EXTRA = "https://richyhbm.co.uk/donate";

    private static final String AUTHOR_ORIGINAL_GITHUB = "https://github.com/0xbb";
    private static final String AUTHOR_ORIGINAL_EXTRA = AUTHOR_ORIGINAL_GITHUB + "/otp-authenticator";

    private static final String CONTRIBUTORS_URI = GITHUB_URI + "/blob/master/README.md#contributors";
    private static final String TRANSLATORS_URI = GITHUB_URI + "/blob/master/README.md#translators";

    private static final String BUGREPORT_URI = GITHUB_URI + "/issues";
    private static final String TRANSLATE_URI = "https://crowdin.com/project/andotp";

    private Settings settings;

    static final int[] imageResources = {
            R.id.aboutImgVersion, R.id.aboutImgLicense, R.id.aboutImgChangelog, R.id.aboutImgSource,
            R.id.aboutImgAuthor2, R.id.aboutImgAuthorOriginal, R.id.aboutImgContributors,
            R.id.aboutImgTranslators, R.id.aboutImgBugs, R.id.aboutImgTranslate
    };

    static long lastTap = 0;
    static int taps = 0;
    static Toast currentToast = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about, container, false);

        settings = new Settings(getActivity());

        ColorFilter filter = Tools.getThemeColorFilter(getActivity(), android.R.attr.textColorSecondary);
        for (int i : imageResources) {
            ImageView imgView = v.findViewById(i);
            imgView.getDrawable().setColorFilter(filter);
        }

        String versionName = "";
        try {
            PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        LinearLayout versionLayout = v.findViewById(R.id.about_layout_version);

        versionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long thisTap = System.currentTimeMillis();

                if (thisTap - lastTap < 500) {
                    taps = taps + 1;

                    if (currentToast != null && taps <= 7)
                        currentToast.cancel();

                    if (taps >= 3 && taps <= 7)
                        currentToast = Toast.makeText(getActivity(), String.valueOf(taps), Toast.LENGTH_SHORT);

                    if (taps == 7) {
                        if (settings.getSpecialFeatures())
                            currentToast = Toast.makeText(getActivity(), R.string.about_toast_special_features_enabled, Toast.LENGTH_LONG);
                        else
                            enableSpecialFeatures();
                    }

                    if (currentToast != null)
                        currentToast.show();
                } else {
                    taps = 0;
                }

                lastTap = thisTap;
            }
        });

        TextView version = v.findViewById(R.id.about_text_version);
        version.setText(versionName);

        LinearLayout license = v.findViewById(R.id.about_layout_license);
        LinearLayout changelog = v.findViewById(R.id.about_layout_changelog);
        LinearLayout source = v.findViewById(R.id.about_layout_source);

        license.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openURI(MIT_URI);
            }
        });
        changelog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openURI(CHANGELOG_URI);
            }
        });
        source.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openURI(GITHUB_URI);
            }
        });

        LinearLayout author1Layout = v.findViewById(R.id.aboutLayoutAuthor1);
        TextView author1Donate = v.findViewById(R.id.about_author1_extra);
        author1Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openURI(AUTHOR1_GITHUB);
            }
        });
        author1Donate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    openURI(AUTHOR1_EXTRA);
                } catch(Exception ignored) {
                    copyToClipboard(AUTHOR1_EXTRA);
                }
            }
        });


        LinearLayout author2Layout = v.findViewById(R.id.aboutLayoutAuthor2);
        TextView author2Donate = v.findViewById(R.id.about_author2_extra);
        author2Layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openURI(AUTHOR2_GITHUB);
            }
        });
        author2Donate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    openURI(AUTHOR2_EXTRA);
                } catch(Exception ignored) {
                    copyToClipboard(AUTHOR2_EXTRA);
                }
            }
        });

        LinearLayout authorOrigialLayout = v.findViewById(R.id.aboutLayoutOriginalAuthor);
        TextView authorOriginalApp = v.findViewById(R.id.about_author_original_extra);
        authorOrigialLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openURI(AUTHOR_ORIGINAL_GITHUB);
            }
        });
        authorOriginalApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    openURI(AUTHOR_ORIGINAL_EXTRA);
                } catch(Exception ignored) {
                    copyToClipboard(AUTHOR_ORIGINAL_EXTRA);
                }
            }
        });

        LinearLayout contributors = v.findViewById(R.id.about_layout_contributors);
        LinearLayout translators = v.findViewById(R.id.about_layout_translators);
        contributors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openURI(CONTRIBUTORS_URI);
            }
        });
        translators.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openURI(TRANSLATORS_URI);
            }
        });

        LinearLayout bugReport = v.findViewById(R.id.about_layout_bugs);
        LinearLayout translate = v.findViewById(R.id.about_layout_translate);
        bugReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openURI(BUGREPORT_URI);
            }
        });
        translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openURI(TRANSLATE_URI);
            }
        });

        return v;
    }

    private void enableSpecialFeatures() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.about_title_special_features)
                .setMessage(R.string.about_dialog_special_features)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        settings.setSpecialFeatures(true);
                        Toast.makeText(getActivity(), R.string.about_toast_special_features, Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .create()
                .show();
    }

    public void openURI(String uri) {
        Intent openURI = new Intent(Intent.ACTION_VIEW);
        openURI.setData(Uri.parse(uri));
        startActivity(openURI);
    }

    public void copyToClipboard(String uri) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("andOTP", uri);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getActivity(), getString(R.string.about_toast_copied_to_clipboard), Toast.LENGTH_SHORT).show();
    }

}
