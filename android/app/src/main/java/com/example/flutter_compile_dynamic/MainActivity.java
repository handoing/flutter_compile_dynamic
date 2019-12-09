package com.example.flutter_compile_dynamic;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.util.List;

import io.flutter.plugins.GeneratedPluginRegistrant;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends HotFixFlutterActivity implements EasyPermissions.PermissionCallbacks,
        EasyPermissions.RationaleCallbacks {

  private static final String[] LOCATION_AND_CONTACTS = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    EasyPermissions.requestPermissions(this, "读写权限", 123, LOCATION_AND_CONTACTS);

    String path = FlutterFileUtils.copyLibAndWrite(MainActivity.this,"libapp.so");

    super.onCreate(savedInstanceState);
    GeneratedPluginRegistrant.registerWith(this);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
  }

  @Override
  public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

  }

  @Override
  public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
    if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
      new AppSettingsDialog.Builder(this).build().show();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {

    }
  }

  @Override
  public void onRationaleAccepted(int requestCode) {

  }

  @Override
  public void onRationaleDenied(int requestCode) {

  }
}
