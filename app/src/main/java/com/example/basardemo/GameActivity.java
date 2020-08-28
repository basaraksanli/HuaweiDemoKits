package com.example.basardemo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.jos.JosApps;
import com.huawei.hms.jos.JosAppsClient;
import com.huawei.hms.jos.games.AchievementsClient;
import com.huawei.hms.jos.games.Games;
import com.huawei.hms.jos.games.PlayersClient;
import com.huawei.hms.jos.games.achievement.Achievement;
import com.huawei.hms.jos.games.buoy.BuoyClient;
import com.huawei.hms.jos.games.player.Player;

import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.result.HuaweiIdAuthResult;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;

import org.json.JSONException;

import java.util.List;


public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    AchievementsClient achievementClient;
    BuoyClient buoyClient;
    private PlayersClient playersClient;
    private String playerID;
    private TextView gameLog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        achievementClient = Games.getAchievementsClient(this);

        buoyClient = Games.getBuoyClient(this);
        gameLog = findViewById(R.id.LogGame);
        initView();
    }

    private void initView() {

        findViewById(R.id.codelab_gameLogin).setOnClickListener(this);
        findViewById(R.id.achievementListButton).setOnClickListener(this);
        findViewById(R.id.achievementListShowButton).setOnClickListener(this);
        findViewById(R.id.unlockachievementButton).setOnClickListener(this);
        findViewById(R.id.codelab_init).setOnClickListener(this);
        findViewById(R.id.codelab_signIn).setOnClickListener(this);
    }

    public HuaweiIdAuthParams getHuaweiIdParams() {

        return new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME).setIdToken().createParams();

    }


    public void signIn() {

        Task<AuthHuaweiId> authHuaweiIdTask = HuaweiIdAuthManager.getService(this, getHuaweiIdParams()).silentSignIn();
        authHuaweiIdTask.addOnSuccessListener(authHuaweiId -> {
            printLog( "silentsignIn success");
            printLog(  "display:" + authHuaweiId.getDisplayName());
            login();
        }).addOnFailureListener(e -> {
            if (e instanceof ApiException) {
                ApiException apiException = (ApiException) e;
                printLog(  "signIn failed:" + apiException.getStatusCode());
                printLog(  "start getSignInIntent");
                HuaweiIdAuthService service = HuaweiIdAuthManager.getService(GameActivity.this, getHuaweiIdParams());
                startActivityForResult(service.getSignInIntent(), 6013);
            }
        });
    }

    public void login() {
        playersClient = Games.getPlayersClient(this);
        Task<Player> playerTask = playersClient.getCurrentPlayer();
        playerTask.addOnSuccessListener(player -> {
            playerID = player.getPlayerId();
            printLog(  "getPlayerInfo Success, player info: " + player.getPlayerId());
        }).addOnFailureListener(e -> {

            if (e instanceof ApiException) {
                printLog(  "getPlayerInfo failed, status: " + ((ApiException) e).getStatusCode());
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        buoyClient.showFloatWindow();
    }

    @Override
    protected void onPause() {
        super.onPause();
        buoyClient.hideFloatWindow();
    }

    private void init() {
        JosAppsClient appsClient = JosApps.getJosAppsClient(this);
        appsClient.init();
        printLog( "init success");
    }

    private void getAchievementList(){
        Task<List<Achievement>> task = achievementClient.getAchievementList(true);
        task.addOnSuccessListener(data -> {
            if (data == null) {
                printLog("achievement list is null");
                return;
            }
            for (Achievement achievement : data) {
                printLog("achievement id" + achievement.getId());
            }
        }).addOnFailureListener(e -> {
            if (e instanceof ApiException) {
                String result = "rtnCode:" +
                        ((ApiException) e).getStatusCode();
                printLog( result);
            }
        });
    }
    private void showAchivementList(){
        Task<Intent> task = achievementClient.getShowAchievementListIntent();
        task.addOnSuccessListener(new OnSuccessListener<Intent>() {
            @Override
            public void onSuccess(Intent intent) {
                if (intent == null) {
                    printLog( "intent = null");
                } else {
                    try {
                        startActivityForResult(intent, 1);
                    } catch (Exception e) {
                        printLog( "Achievement Activity is Invalid");
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    String result = "rtnCode:"
                            + ((ApiException) e).getStatusCode();
                    printLog( "result:" +result);
                }
            }
        });
    }
    private void unlockAchievement(String achievementID){
        Task<Void> task = achievementClient.reachWithResult(achievementID);
        task.addOnSuccessListener(v -> printLog("reach  success")).addOnFailureListener(e -> {
            if (e instanceof ApiException) {
                String result = "rtnCode:"
                        + ((ApiException) e).getStatusCode();
                printLog("reach result" + result);
            }
        });
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.codelab_gameLogin) {

            login();
        }
        if (v.getId() == R.id.achievementListButton) {

            getAchievementList();
        }
        if (v.getId() == R.id.achievementListShowButton) {

            showAchivementList();
        }
        if (v.getId() == R.id.unlockachievementButton) {

            unlockAchievement("2B90F1A8D3C79906225A5035083F41BE9494B78B900EE6E19A26D55298AA92B3");
        }
        if (v.getId() == R.id.codelab_init) {

            init();
        }
        if (v.getId() == R.id.codelab_signIn) {

            signIn();
        }
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 6013) {

            if (null == data) {
                printLog( "signIn intent is null");
                return;
            }
            String jsonSignInResult = data.getStringExtra("HUAWEIID_SIGNIN_RESULT");
            if (TextUtils.isEmpty(jsonSignInResult)) {
                printLog( "signIn result is empty");
                return;
            }
            try {
                HuaweiIdAuthResult signInResult = new HuaweiIdAuthResult().fromJson(jsonSignInResult);
                if (0 == signInResult.getStatus().getStatusCode()) {
                    printLog( "signIn success.");
                    printLog( "signIn result: " + signInResult.toJson());
                } else {
                    printLog( "signIn failed: " + signInResult.getStatus().getStatusCode());
                }
            } catch (JSONException var7) {
                printLog( "Failed to convert json from signInResult.");
            }
        }
    }
    public void printLog(String s){
        gameLog.append("--- "+s + "\n");
    }
}