package com.jennyni.skindemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;


public abstract class PermissionsActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 0x114; // 系统权限管理页面的参数
    private static final String PACKAGE_URL_SCHEME = "package:"; // 方案


    private PermissionsChecker mPermissionsChecker; // 权限检测器
    private boolean isRequireCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPermissionsChecker = new PermissionsChecker(this);
    }

    protected void requestPermission(){
        isRequireCheck = true;
        if (isRequireCheck) {
            String[] permissions = getPermission();
            if (mPermissionsChecker.lacksPermissions(permissions)) {
                requestPermissions(permissions); // 请求权限
            } else {
                // 全部权限都已获取
                onPermissionRequestSuccess();
            }
        }
    }


    /**
     * 用户权限处理,
     * 如果全部获取, 则直接过.
     * 如果权限缺失, 则提示Dialog.
     *
     * @param requestCode  请求码
     * @param permissions  权限
     * @param grantResults 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && hasAllPermissionsGranted(grantResults)) {
            isRequireCheck = true;
            // 全部权限都已获取
            onPermissionRequestSuccess();
        } else {
            isRequireCheck = false;
            onPermissionRequestFail();
        }
    }

    // 请求权限兼容低版本
    private void requestPermissions(String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    // 含有全部的权限
    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    // 显示缺失权限提示
    protected void showMissingPermissionDialog(String tip) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage(tip);
        builder.setCancelable(false);
        // 拒绝, 退出应用
        builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.setPositiveButton("去开启", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startAppSettings();
                finish();
            }
        });

        builder.show();
    }

    // 启动应用的设置
    public void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
        startActivity(intent);
    }

    public abstract String[] getPermission();

    public abstract void onPermissionRequestSuccess();

    public abstract void onPermissionRequestFail();


}
