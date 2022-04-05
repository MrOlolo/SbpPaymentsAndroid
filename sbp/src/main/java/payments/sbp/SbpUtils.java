package payments.sbp;

import static androidx.core.content.res.ResourcesCompat.getDrawable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import payments.sbp.repository.Repository;
import payments.sbp.repository.remote.RemoteRepository;
import payments.sbp.ui.BankAppsAdapter;

public class SbpUtils {

    private static final String APP_SEPARATOR = ";";
    private static SbpUtils instance;
    /**
     * variable for holding known bank apps packages
     */
    private String predefinedApps = "com.idamob.tinkoff.android;com.openbank;com.webmoney.my;ru.mkb.mobile;ru.vtb24.mobilebanking.android;logo.com.mbanking;ru.skbbank.ib;ru.wildberries.razz";
    // private Activity context;
    private BottomSheetDialog dialog;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private SbpUtils() {
        // this.context = context;
        Repository repository = RemoteRepository.getInstance();
        repository.loadAppPackages(this::onPackages);

    }

    public static SbpUtils getInstance() {
        if (instance == null) {
            instance = new SbpUtils();
        }
        return instance;
    }

    private Boolean isDarkColor(int colorCode) {
        double a = (0.2126 * Color.red(colorCode) + 0.7152 * Color.green(colorCode) + 0.0722 * Color.blue(colorCode)) / 255;
        return a < 0.5;
    }

    public void onPackages(List<String> packages) {
        StringBuilder builder = new StringBuilder();
        for (String app : packages)
            builder.append(app).append(APP_SEPARATOR);
        updateBankApps(builder.toString());
    }

    public void onClick(Activity context, String link, ResolveInfo info) {
        handler.postDelayed(this::hideDialog, 500);
        if (TextUtils.isEmpty(link))
            return;
        startBankApp(context, link, info);

    }

    public void hideDialog() {
        if (dialog != null && dialog.isShowing())
            dialog.cancel();
        dialog = null;
    }

    public void cleanup() {
        hideDialog();
        handler.removeCallbacksAndMessages(null);

    }

    //if there is source, list of bank apps packages could be loaded from, default list could be overridden
    public void updateBankApps(String apps) {
        predefinedApps = apps;
    }

    /**
     * return list of installed bank apps
     *
     * @param link pyment url we've got from QR ("https://qr.nspk.ru/......")
     */
    public List<ResolveInfo> getBankApps(Activity context, String link) {
        //STEP 1: LOADING BANK APPS BY PROVIDED LINK

        //load list of apps that can  process  url "http://". Usually it is installed browsers
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://"));
        PackageManager pm = context.getApplicationContext().getPackageManager();
        if (pm == null)
            return new ArrayList<>();
        List<ResolveInfo> browsers = pm.queryIntentActivities(browserIntent, 0);
        //list of resolved browser packages
        final List<String> browserPackages = new ArrayList<>();
        if (browsers != null)
            for (ResolveInfo info : browsers)
                browserPackages.add(info.activityInfo.packageName);
        // load list of apps that can  process   provided url "https://qr.nspk.ru...".
        //as a result we will have mix of bank apps and browsers
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(link);
        intent.setDataAndNormalize(uri);
        List<ResolveInfo> allApps = pm.queryIntentActivities(intent, 0);
        //bank apps filtering
        List<ResolveInfo> resolvedPrintList = new ArrayList<>();
        List<String> appsPackages = new ArrayList<>();
        for (ResolveInfo app : allApps) {
            // Use condition to filter the List
            if (!browserPackages.contains(app.activityInfo.packageName)) {
                resolvedPrintList.add(app);
                appsPackages.add(app.activityInfo.packageName);
            }
        }

        //STEP 2: LOADING BANK APPS BY PREDEFINED PACKAGES
        //get list of all installed applications
        @SuppressLint("QueryPermissionsNeeded") List<ApplicationInfo> packages = pm.getInstalledApplications(0);
        List<String> predefinedPackages = Arrays.asList(predefinedApps.split(APP_SEPARATOR));
        //Filter installed bank apps
        for (ApplicationInfo packageInfo : packages) {
            if (predefinedPackages.contains(packageInfo.packageName)
                    && !appsPackages.contains(packageInfo.packageName)) {
                // if (TextUtils.isEmpty(packageInfo.className))
                //    continue;
                Intent i = new Intent();
                i.setPackage(packageInfo.packageName);
                i.addCategory(Intent.CATEGORY_LAUNCHER);

                ResolveInfo app = pm.resolveActivity(i, 0);
                if (app == null)
                    continue;
                resolvedPrintList.add(app);
            }

        }

        // Log.d(SbpUtils.class.getSimpleName(), "APPS: " + appsPackages);
        return resolvedPrintList;
    }

    /**
     * start bank app for process payment, usually after selecting particular app from list
     *
     * @param link payment url we've got from QR ("https://qr.nspk.ru/......")
     * @param info selected bank app ResolveInfo
     */
    public void startBankApp(Activity context, String link, @NonNull ResolveInfo info) {
        if (TextUtils.isEmpty(link))
            return;

        Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(link));

        intent1.setClassName(info.activityInfo.packageName, info.activityInfo.name);
        context.startActivity(intent1);

    }

    /**
     * open dialog with available bank applications. After user select one, payment  process will be started
     *
     * @param link payment url we've got from QR ("https://qr.nspk.ru/......") shold have parameter "redirect"
     */
    public void showSbpListDialog(Activity context, String link) {
        showSbpListDialog(context, link, 0xFF000000, 0xFFFFFFFF);
    }

    /**
     * open dialog with available bank applications. After user select one, payment  process will be started
     *
     * @param link payment url we've got from QR ("https://qr.nspk.ru/......") shold have parameter "redirect"
     */
    public void showSbpListDialog(Activity context, String link, int textCode, int backgroundCode) {
        List<ResolveInfo> bankApps = getBankApps(context, link);


        BankAppsAdapter appsAdapter = new BankAppsAdapter(this::onClick, link, textCode);
        appsAdapter.setApps(bankApps);
        hideDialog();
        @SuppressLint("InflateParams") View dialogView = context.getLayoutInflater().inflate(R.layout.dialog_bank_apps, null);

        dialog = new BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme);
        dialog.setCancelable(true);

        dialog.setOnShowListener(dialog1 -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog1;

            FrameLayout container = d.findViewById(com.google.android.material.R.id.container);
            if (container == null)
                return;
            TextView tvHint = dialogView.findViewById(R.id.tvHint);
            ((ViewGroup) tvHint.getParent()).removeView(tvHint);
            container.addView(tvHint);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) tvHint.getLayoutParams();
            lp.gravity = Gravity.BOTTOM;

        });

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getDrawable(context.getResources(), R.drawable.app_item_divider, null));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(appsAdapter);
        recyclerView.setVisibility(bankApps.isEmpty() ? View.GONE : View.VISIBLE);
        View emptyView = dialogView.findViewById(R.id.tvNoApps);
        ((TextView) dialogView.findViewById(R.id.tvNoApps)).setTextColor(textCode);
        ((TextView) dialogView.findViewById(R.id.tvCompletePayment)).setTextColor(textCode);
        emptyView.setVisibility(bankApps.isEmpty() ? View.VISIBLE : View.GONE);
        dialogView.findViewById(R.id.tvHint).setBackgroundColor(backgroundCode);
        ((TextView) dialogView.findViewById(R.id.tvHint)).setTextColor(textCode);
        dialog.setContentView(dialogView);
        @SuppressLint("UseCompatLoadingForDrawables") Drawable customDrawable = context.getResources().getDrawable(R.drawable.rounded_dialog);
        customDrawable.setColorFilter(backgroundCode, PorterDuff.Mode.SRC_IN);

        if (isDarkColor(backgroundCode)) {
            ((ImageView) dialogView.findViewById(R.id.imgSbp)).setImageResource(R.drawable.sbp_light_logo);
        }
        dialogView.setBackground(customDrawable);

//        ((View) dialogView.getParent()).se()
//        ((View) dialogView.getParent()).setBackgroundColor(0xFF293232);
        dialog.show();
    }
}
