package com.example.basardemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.cloud.base.auth.DriveCredential;
import com.huawei.cloud.base.http.FileContent;
import com.huawei.cloud.base.media.MediaHttpDownloader;
import com.huawei.cloud.base.util.StringUtils;
import com.huawei.cloud.client.exception.DriveCode;
import com.huawei.cloud.services.drive.Drive;
import com.huawei.cloud.services.drive.DriveScopes;
import com.huawei.cloud.services.drive.model.Comment;
import com.huawei.cloud.services.drive.model.CommentList;
import com.huawei.cloud.services.drive.model.File;
import com.huawei.cloud.services.drive.model.FileList;
import com.huawei.cloud.services.drive.model.HistoryVersionList;
import com.huawei.cloud.services.drive.model.Reply;
import com.huawei.cloud.services.drive.model.ReplyList;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.api.entity.auth.Scope;
import com.huawei.hms.support.hwid.HuaweiIdAuthAPIManager;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.huawei.hms.support.hwid.request.HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM;

public class DriveActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "MainActivity";

    private static int REQUEST_SIGN_IN_LOGIN = 1002;

    private DriveCredential mCredential;
    // huawei account AT
    private String accessToken;

    private File directoryCreated;
    private File fileUploaded;
    private File fileSearched;
    private Comment mComment;
    private Reply mReply;

    private CheckBox isApplicationData;
    private EditText uploadFileName;
    private EditText searchFileName;
    private EditText commentText;
    private EditText replyText;
    private TextView queryResult;
    private TextView commentList;
    private TextView replyList;
    private TextView historyVersionList;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private static final Map<String, String> MIME_TYPE_MAP = new HashMap<>();

    static {
        MIME_TYPE_MAP.put(".doc", "application/msword");
        MIME_TYPE_MAP.put(".jpg", "image/jpeg");
        MIME_TYPE_MAP.put(".mp3", "audio/x-mpeg");
        MIME_TYPE_MAP.put(".mp4", "video/mp4");
        MIME_TYPE_MAP.put(".pdf", "application/pdf");
        MIME_TYPE_MAP.put(".png", "image/png");
        MIME_TYPE_MAP.put(".txt", "text/plain");
    }

    private DriveCredential.AccessMethod refreshAT = new DriveCredential.AccessMethod() {
        @Override
        public String refreshToken() {
            return accessToken;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);

        requestPermissions(PERMISSIONS_STORAGE, 1);

        uploadFileName = findViewById(R.id.uploadFileName);
        searchFileName = findViewById(R.id.searchFileName);
        isApplicationData = findViewById(R.id.isApplicationData);
        queryResult = findViewById(R.id.queryResult);
        commentList = findViewById(R.id.commentList);
        replyList = findViewById(R.id.replyList);
        commentText = findViewById(R.id.commentText);
        replyText = findViewById(R.id.replyText);
        historyVersionList = findViewById(R.id.historyVersionList);
        findViewById(R.id.buttonLogin).setOnClickListener(this);
        findViewById(R.id.buttonUploadFiles).setOnClickListener(this);
        findViewById(R.id.buttonQueryFiles).setOnClickListener(this);
        findViewById(R.id.buttonDownloadFiles).setOnClickListener(this);
        findViewById(R.id.buttonCreateComment).setOnClickListener(this);
        findViewById(R.id.buttonQueryComment).setOnClickListener(this);
        findViewById(R.id.buttonCreateReply).setOnClickListener(this);
        findViewById(R.id.buttonQueryReply).setOnClickListener(this);
        findViewById(R.id.buttonQueryHistoryVersion).setOnClickListener(this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult, requestCode = " + requestCode + ", resultCode = " + resultCode);
        if (requestCode == REQUEST_SIGN_IN_LOGIN) {
            Task<AuthHuaweiId> authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data);
            if (authHuaweiIdTask.isSuccessful()) {
                AuthHuaweiId huaweiAccount = authHuaweiIdTask.getResult();
                accessToken = huaweiAccount.getAccessToken();
                String unionId = huaweiAccount.getUnionId();
                int returnCode = init(unionId, accessToken, refreshAT);
                if (DriveCode.SUCCESS == returnCode) {
                    showTips("login ok");
                } else if (DriveCode.SERVICE_URL_NOT_ENABLED == returnCode) {
                    showTips("drive is not enabled");
                } else {
                    showTips("login error");
                }
            } else {
                Log.d(TAG, "onActivityResult, signIn failed: " + ((ApiException) authHuaweiIdTask.getException()).getStatusCode());
                Toast.makeText(getApplicationContext(), "onActivityResult, signIn failed.", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void showTips(final String toastText) {
        DriveActivity.this.runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_LONG).show();
            TextView textView = findViewById(R.id.textView);
            textView.setText(toastText);
        });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonLogin:
                driveLogin();
                break;
            case R.id.buttonUploadFiles:
                uploadFiles();
                break;
            case R.id.buttonQueryFiles:
                queryFiles();
                break;
            case R.id.buttonDownloadFiles:
                downloadFiles();
                break;
            case R.id.buttonCreateComment:
                createComment();
                break;
            case R.id.buttonQueryComment:
                queryComment();
                break;
            case R.id.buttonCreateReply:
                createReply();
                break;
            case R.id.buttonQueryReply:
                queryReply();
                break;
            case R.id.buttonQueryHistoryVersion:
                queryHistoryVersion();
            default:
                break;
        }
    }
    private void driveLogin() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        List<Scope> scopeList = new ArrayList<>();
        scopeList.add(new Scope(DriveScopes.SCOPE_DRIVE));
        scopeList.add(new Scope(DriveScopes.SCOPE_DRIVE_READONLY));
        scopeList.add(new Scope(DriveScopes.SCOPE_DRIVE_FILE));
        scopeList.add(new Scope(DriveScopes.SCOPE_DRIVE_METADATA));
        scopeList.add(new Scope(DriveScopes.SCOPE_DRIVE_METADATA_READONLY));
        scopeList.add(new Scope(DriveScopes.SCOPE_DRIVE_APPDATA));
        scopeList.add(HuaweiIdAuthAPIManager.HUAWEIID_BASE_SCOPE);

        HuaweiIdAuthParams authParams = new HuaweiIdAuthParamsHelper(DEFAULT_AUTH_REQUEST_PARAM)
                .setAccessToken()
                .setIdToken()
                .setScopeList(scopeList)
                .createParams();
        HuaweiIdAuthService client = HuaweiIdAuthManager.getService(this, authParams);
        startActivityForResult(client.getSignInIntent(), REQUEST_SIGN_IN_LOGIN);
    }

    private void uploadFiles() {
        new Thread(() -> {
            try {
                if (accessToken == null) {
                    showTips("please click 'Login'.");
                    return;
                }
                if (StringUtils.isNullOrEmpty(uploadFileName.getText().toString())) {
                    showTips("please input upload file name above.");
                    return;
                }
                java.io.File fileObject = new java.io.File( Environment.getExternalStorageDirectory() +"/"+ uploadFileName.getText());
                if (!fileObject.exists()) {
                    showTips("the input file does not exit.");
                    return;
                }
                Drive drive = buildDrive();
                Map<String, String> appProperties = new HashMap<>();
                appProperties.put("appProperties", "property");
                // create somepath directory
                File file = new File();
                file.setFileName("somepath" + System.currentTimeMillis())
                        .setMimeType("application/vnd.huawei-apps.folder")
                        .setAppSettings(appProperties);
                if (isApplicationData.isChecked()) {
                    file.setParentFolder(Collections.singletonList("applicationData"));
                }
                directoryCreated = drive.files().create(file).execute();
                // create test.jpg on cloud
                String mimeType = mimeType(fileObject);
                File content = new File()
                        .setFileName(fileObject.getName())
                        .setMimeType(mimeType)
                        .setParentFolder(Collections.singletonList(directoryCreated.getId()));
                fileUploaded = drive.files()
                        .create(content, new FileContent(mimeType, fileObject))
                        .setFields("*")
                        .execute();
                showTips("upload success");
            } catch (Exception ex) {
                Log.d(TAG, "upload error " + ex.toString());
                showTips("upload error " + ex.toString());
            }
        }).start();
    }

    private void queryFiles() {
        new Thread(() -> {
            try {
                if (accessToken == null) {
                    showTips("please click 'Login'.");
                    return;
                }
                if (StringUtils.isNullOrEmpty(searchFileName.getText().toString())) {
                    showTips("please input file name above.");
                    return;
                }
                String containers = "";
                String queryFile = "fileName = '" + searchFileName.getText() + "' and mimeType != 'application/vnd.huawei-apps.folder'";
                if (isApplicationData.isChecked()) {
                    containers = "applicationData";
                    queryFile = "'applicationData' in parentFolder and ".concat(queryFile);
                }
                Drive drive = buildDrive();
                Drive.Files.List request = drive.files().list();
                FileList files;
                while (true) {
                    files = request
                            .setQueryParam(queryFile)
                            .setPageSize(10)
                            .setOrderBy("fileName")
                            .setFields("category,nextCursor,files(id,fileName,size)")
                            .setContainers(containers)
                            .execute();
                    if (files == null || files.getFiles().size() > 0) {
                        break;
                    }
                    if (!StringUtils.isNullOrEmpty(files.getNextCursor())) {
                        request.setCursor(files.getNextCursor());
                    } else {
                        break;
                    }
                }
                String text;
                if (files != null && files.getFiles().size() > 0) {
                    fileSearched = files.getFiles().get(0);
                    text = fileSearched.toString();
                } else {
                    text = "empty";
                }
                final String finalText = text;
                DriveActivity.this.runOnUiThread(() -> queryResult.setText(finalText));
                showTips("query ok");
            } catch (Exception ex) {
                Log.d(TAG, "query error " + ex.toString());
                showTips("query error " + ex.toString());
            }
        }).start();
    }

    private void downloadFiles() {
        new Thread(() -> {
            try {
                if (accessToken == null) {
                    showTips("please click 'Login'.");
                    return;
                }
                if (fileSearched == null) {
                    showTips("please click 'QUERY FILE'.");
                    return;
                }
                Drive drive = buildDrive();
                File content = new File();
                Drive.Files.Get request = drive.files().get(fileSearched.getId());
                content.setFileName(fileSearched.getFileName()).setId(fileSearched.getId());
                MediaHttpDownloader downloader = request.getMediaHttpDownloader();
                downloader.setContentRange(0, fileSearched.getSize() - 1);
                String filePath = "/storage/emulated/0/Huawei/CloudDrive/DownLoad/Demo_" + fileSearched.getFileName();
                request.executeContentAndDownloadTo(new FileOutputStream(new java.io.File(filePath)));
                showTips("download to " + filePath);
            } catch (Exception ex) {
                Log.d(TAG, "download error " + ex.toString());
                showTips("download error " + ex.toString());
            }
        }).start();
    }

    private void createComment() {
        new Thread(() -> {
            try {
                if (accessToken == null) {
                    showTips("please click 'Login'.");
                    return;
                }
                if (fileSearched == null) {
                    showTips("please click 'QUERY FILE'.");
                    return;
                }
                if (StringUtils.isNullOrEmpty(commentText.getText().toString())) {
                    showTips("please input comment above.");
                    return;
                }
                Drive drive = buildDrive();
                Comment comment = new Comment();
                comment.setDescription(commentText.getText().toString());
                mComment = drive.comments()
                        .create(fileSearched.getId(), comment)
                        .setFields("*")
                        .execute();

                if (mComment != null && mComment.getId() != null) {
                    Log.i(TAG, "Add comment success");
                    showTips("Add comment success");
                } else {
                    Log.e(TAG, "Add comment failed");
                    showTips("Add comment failed");
                }
            } catch (Exception ex) {
                Log.d(TAG, "Add comment error " + ex.toString());
                showTips("Add comment error");
            }
        }).start();
    }

    private void queryComment() {
        new Thread(() -> {
            try {
                if (accessToken == null) {
                    showTips("please click 'Login'.");
                    return;
                }
                if (fileSearched == null) {
                    showTips("please click 'QUERY FILE'.");
                    return;
                }
                Drive drive = buildDrive();
                CommentList response = drive.comments()
                        .list(fileSearched.getId())
                        .setFields("comments(id,description,replies(description))")
                        .execute();

                final String text = response.getComments().toString();
                DriveActivity.this.runOnUiThread(() -> commentList.setText(text));
            } catch (Exception ex) {
                Log.d(TAG, "query comment error " + ex.toString());
                showTips("query comment error");
            }
        }).start();
    }

    private void createReply() {
        new Thread(() -> {
            try {
                if (accessToken == null) {
                    showTips("please click 'Login'.");
                    return;
                }
                if (fileSearched == null) {
                    showTips("please click 'QUERY FILE'.");
                    return;
                }
                if (mComment == null) {
                    showTips("please click 'COMMENT THE FILE'.");
                    return;
                }
                if (StringUtils.isNullOrEmpty(replyText.getText().toString())) {
                    showTips("please input comment above.");
                    return;
                }
                Drive drive = buildDrive();
                Reply reply = new Reply();
                reply.setDescription(replyText.getText().toString());
                mReply = drive.replies()
                        .create(fileSearched.getId(), mComment.getId(), reply)
                        .setFields("*")
                        .execute();

                if (mReply != null && mReply.getId() != null) {
                    Log.i(TAG, "Add reply success");
                    showTips("Add reply success");
                } else {
                    Log.e(TAG, "Add reply failed");
                    showTips("Add reply failed");
                }
            } catch (Exception ex) {
                Log.d(TAG, "Add reply error " + ex.toString());
                showTips("Add reply error");
            }
        }).start();
    }

    private void queryReply() {
        new Thread(() -> {
            try {
                if (accessToken == null) {
                    showTips("please click 'Login'.");
                    return;
                }
                if (fileSearched == null) {
                    showTips("please click 'QUERY FILE'.");
                    return;
                }
                if (mComment == null) {
                    showTips("please click 'COMMENT THE FILE'.");
                    return;
                }
                Drive drive = buildDrive();
                ReplyList response = drive.replies()
                        .list(fileSearched.getId(), mComment.getId())
                        .setFields("replies(id,description)")
                        .execute();

                final String text = response.getReplies().toString();
                DriveActivity.this.runOnUiThread(() -> replyList.setText(text));
            } catch (Exception ex) {
                Log.d(TAG, "query reply error " + ex.toString());
                showTips("query reply error");
            }
        }).start();
    }

    private void queryHistoryVersion() {
        new Thread(() -> {
            try {
                if (accessToken == null) {
                    showTips("please click 'Login'.");
                    return;
                }
                if (fileSearched == null) {
                    showTips("please click 'QUERY FILE'.");
                    return;
                }
                Drive drive = buildDrive();
                HistoryVersionList response = drive.historyVersions()
                        .list(fileSearched.getId())
                        .setFields("historyVersions(id,sha256)")
                        .execute();

                final String text = response.getHistoryVersions().toString();
                DriveActivity.this.runOnUiThread(() -> historyVersionList.setText(text));
            } catch (Exception ex) {
                Log.d(TAG, "query historyVersion", ex);
                showTips("query historyVersion error");
            }
        }).start();
    }

    private Drive buildDrive() {
        return new Drive.Builder(mCredential, this).build();
    }

    private String mimeType(java.io.File file) {
        if (file != null && file.exists() && file.getName().contains(".")) {
            String fileName = file.getName();
            String suffix = fileName.substring(fileName.lastIndexOf("."));
            if (MIME_TYPE_MAP.containsKey(suffix)) {
                return MIME_TYPE_MAP.get(suffix);
            }
        }
        return "*/*";
    }

    public int init(String unionID, String at, DriveCredential.AccessMethod refreshAT) {
        if (StringUtils.isNullOrEmpty(unionID) || StringUtils.isNullOrEmpty(at)) {
            return DriveCode.ERROR;
        }
        DriveCredential.Builder builder = new DriveCredential.Builder(unionID, refreshAT);
        mCredential = builder.build().setAccessToken(at);
        return DriveCode.SUCCESS;
    }
}