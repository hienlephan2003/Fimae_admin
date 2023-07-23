package com.example.fimae.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fimae.R;
import com.example.fimae.models.Report;
import com.example.fimae.repository.ConnectRepo;
import com.example.fimae.service.TimerService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.stringee.call.StringeeCall2;
import com.stringee.common.StringeeAudioManager;
import com.stringee.listener.StatusListener;
import com.stringee.video.StringeeVideoTrack;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CallVideoActivity extends AppCompatActivity {

    private int TIME_CALL = 5 * 60;

    private FrameLayout frmTextDes;

    private TextView tvStatus;
    private View vIncoming;
    private View vOption;
    private FrameLayout vLocal;
    private FrameLayout vRemote;
    private ImageButton btnSpeaker;
    private ImageButton btnMute;
    private ImageButton btnVideo;
    private ImageButton btnSwitch;
    private ImageButton btnAnswer;
    private ImageButton btnReject;
    private ImageButton btnEnd;

    private StringeeCall2 call;

    private boolean isInComingCall = false;
    private String to;
    private String callId;

    private StringeeCall2.SignalingState mSignalingState;
    private StringeeCall2.MediaState mMediaState;

    // audio
    private StringeeAudioManager audioManager;

    // check trang thai speaker and mic
    private boolean isSpeaker = false;
    private boolean isMicOn = true;
    private boolean isVideoOn = true;

    // like
    private boolean isLiked = false;

    // Appbar
    private ImageButton btnClose;
    private ImageButton btnReport;
    private LinearLayout layoutTimer;
    private TimerService timerService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_video);

        tvStatus = findViewById(R.id.tv_status_vid);
        vIncoming = findViewById(R.id.v_incoming_vid);
        vLocal = findViewById(R.id.v_local);
        vRemote = findViewById(R.id.v_remote);
        vOption = findViewById(R.id.v_option_vid);
        btnAnswer = findViewById(R.id.btn_answer_vid);
        btnSpeaker = findViewById(R.id.btn_speaker_vid);
        btnMute = findViewById(R.id.btn_mute_vid);
        btnReject = findViewById(R.id.btn_reject_vid);
        btnEnd = findViewById(R.id.btn_end_vid);
        btnVideo = findViewById(R.id.btn_video);
        btnSwitch = findViewById(R.id.btn_switch);
        frmTextDes = findViewById(R.id.frame_text_des);

        btnSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(() -> {
                    if(audioManager != null) {
                        audioManager.setSpeakerphoneOn(!isSpeaker);
                        isSpeaker = !isSpeaker;
                        btnSpeaker.setBackgroundResource(isSpeaker? R.drawable.background_btn_speaker_on : R.drawable.background_btn_speaker_off);
                    }
                });
            }
        });
        btnMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(() -> {
                    if(call != null){
                        call.mute(isMicOn);
                        isMicOn = !isMicOn;
                        btnMute.setBackgroundResource(isMicOn? R.drawable.background_btn_mic_on : R.drawable.background_btn_mic_off);
                    }
                });
            }
        });
        btnAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(() -> {
                    if(call != null){
                        call.answer(new StatusListener() {
                            @Override
                            public void onSuccess() {

                            }
                        });
                        vIncoming.setVisibility(View.GONE);
                        vOption.setVisibility(View.VISIBLE);
                        btnEnd.setVisibility(View.VISIBLE);
                        frmTextDes.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(() -> {
                    if(call != null){
                        call.reject(new StatusListener() {
                            @Override
                            public void onSuccess() {

                            }
                        });
                        onFinish();
                    }
                });
            }
        });
        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isLiked) {
                    // neu chua like
                    onLiked();
                    // delete timer
                    timerService.onDestroy();
                    layoutTimer.setVisibility(View.GONE);
                }
                else {
                    // cup may
                    timerService.onDestroy();
                    onEndCall();
                }
            }
        });

        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(call != null){
                    call.switchCamera(new StatusListener() {
                        @Override
                        public void onSuccess() {

                        }
                    });
                }
            }
        });
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call.enableVideo(!isVideoOn);
                isVideoOn = !isVideoOn;
                btnVideo.setBackgroundResource(isVideoOn? R.drawable.background_btn_videocam_on : R.drawable.background_btn_videocam_off);
            }
        });

        if(getIntent() != null){
            isInComingCall = getIntent().getBooleanExtra("isIncomingCall", false);
            to = getIntent().getStringExtra("to");
            // duoc goi
            callId = getIntent().getStringExtra("callId");
        }

        // kiem tra dang goi den
        vIncoming.setVisibility(isInComingCall? View.VISIBLE : View.GONE);
        vOption.setVisibility(isInComingCall? View.GONE: View.VISIBLE);
        btnEnd.setVisibility(isInComingCall? View.GONE: View.VISIBLE);
        frmTextDes.setVisibility(isInComingCall? View.GONE: View.VISIBLE);

        // list permission
        List<String> listPermission = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // add permisson
            listPermission.add(Manifest.permission.RECORD_AUDIO);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // add permisson
            listPermission.add(Manifest.permission.CAMERA);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // add permisson
                listPermission.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }
        if(listPermission.size() > 0){
            String[] permissions = new String[listPermission.size()];
            for(int i = 0; i < listPermission.size(); i++){
                permissions[i] = listPermission.get(i);
            }
            ActivityCompat.requestPermissions(this, permissions, 0);
            return;
        }

        initCall();

        // appbar ==================================================================
        btnClose = findViewById(R.id.btn_close_appbar);
        btnReport = findViewById(R.id.btn_report_appbar);
        btnClose.setBackgroundResource(R.drawable.ic_logout);

        btnClose.setOnClickListener(v -> {
            // cup may
            timerService.onDestroy();
            onEndCall();
        });

        btnReport.setOnClickListener(v -> {
            // report
            showReport();
        });

        // timer ==================================================================
        layoutTimer = findViewById(R.id.layout_timer);

        timerService = new TimerService(
                TIME_CALL,
                findViewById(R.id.pbTimer),
                findViewById(R.id.tv_time_connect),
                new TimerService.IOnTimeUp() {
                    @Override
                    public void onTimeUp() {
                        if(!isLiked) {
                            // neu chua like thi dung khi het thoi gian
                            onEndCall();
                            timerService.onDestroy();
                        }
                        else {
                            // neu like roi thi an di
                            layoutTimer.setVisibility(View.GONE);
                        }
                    }
                }
        );
        timerService.setTimeInit();
        timerService.startTimerSetUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerService.onDestroy();
    }

    private void onLiked() {
        // doi background button call
        // an di frame_text_like
        // doi text tv_des_call
        // doi bien like
        isLiked = true;
        frmTextDes.setVisibility(View.GONE);
        btnEnd.setBackgroundResource(R.drawable.background_btn_call);

    }
    // call =======================================================================

    private void onEndCall(){
        if(call != null){
            call.hangup(new StatusListener(){
                @Override
                public void onSuccess() {

                }
            });
            onFinish();
        }
    }

    private void onFinish() {
        audioManager.stop();
        WaitingActivity.isCalled = false;
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        //finish();
    }

    // lay token de thuc hien cuoc goi
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // lay quyen audio
        boolean isGranted = false;
        if(grantResults.length > 0){
            for(int grantResult : grantResults){
                // check trang thai quyen nguoi dung cho phep
                if(grantResult != PackageManager.PERMISSION_GRANTED){
                    isGranted = false;
                    break;
                }else{
                    isGranted = true;
                }
            }
        }
        // check nguoi dung cap quyen chua
        if(requestCode == 0){
            if(!isGranted){
                onFinish();
            } else {
                initCall();
            }
        }
    }

    private void initCall(){
        if(isInComingCall){
            // cuoc goi den
            call = WaitingActivity.call2Map.get(callId);
            if( call == null){
                onFinish();
                return;
            }
        }else{
            // tao cuoc goi moi
            call = new StringeeCall2(WaitingActivity.client, WaitingActivity.client.getUserId(), to);
            call.setVideoCall(true);
        }

        // theo doi trang thai cuoc goi
        call.setCallListener(new StringeeCall2.StringeeCallListener() {
            @Override
            public void onSignalingStateChange(StringeeCall2 stringeeCall2, StringeeCall2.SignalingState signalingState, String s, int i, String s1) {
                // trang thai dieu huong cuoc goi
                // khi nao bat dau, ket thuc
                runOnUiThread(()->{
                    mSignalingState = signalingState;
                    switch (signalingState) {
                        case CALLING:
                            tvStatus.setText("Đang gọi");
                            break;
                        case RINGING:
                            tvStatus.setText("Đang đổ chuông");
                            break;
                        case ANSWERED:
                            tvStatus.setText("Đang trả lời");
                            // cuoc goi bat dau
                            if(mMediaState == StringeeCall2.MediaState.CONNECTED){
                                tvStatus.setText("");
                            }
                            break;
                        case BUSY:
                            tvStatus.setText("Máy bận");
                            onFinish();
                            break;
                        case ENDED:
                            tvStatus.setText("Kết thúc");
                            onFinish();
                            break;
                    }
                });
            }

            @Override
            public void onError(StringeeCall2 stringeeCall2, int i, String s) {
                // cuoc goi bi loi
                runOnUiThread(()->{
                    tvStatus.setText("Lỗi đường truyền");
                    onFinish();
                });
            }

            @Override
            public void onHandledOnAnotherDevice(StringeeCall2 stringeeCall2, StringeeCall2.SignalingState signalingState, String s) {

            }

            @Override
            public void onMediaStateChange(StringeeCall2 stringeeCall2, StringeeCall2.MediaState mediaState) {
                // khi nao co media connected
                runOnUiThread(()->{
                    mMediaState = mediaState;
                    if(mediaState == StringeeCall2.MediaState.CONNECTED){
                        if(mSignalingState == StringeeCall2.SignalingState.ANSWERED){
                            tvStatus.setText("");
                        }
                    }else{
                        // mat ket noi
                        tvStatus.setText("Đang kết nối lại");
                    }
                });
            }

            @Override
            public void onLocalStream(StringeeCall2 stringeeCall2) {
                runOnUiThread(() -> {
                    vLocal.removeAllViews();
                    vLocal.addView(stringeeCall2.getLocalView());
                    stringeeCall2.renderLocalView(true);
                });
            }

            @Override
            public void onRemoteStream(StringeeCall2 stringeeCall2) {
                runOnUiThread(() -> {
                    vRemote.removeAllViews();
                    vRemote.addView(stringeeCall2.getRemoteView());
                    stringeeCall2.renderRemoteView(false);
                });
            }

            @Override
            public void onVideoTrackAdded(StringeeVideoTrack stringeeVideoTrack) {

            }

            @Override
            public void onVideoTrackRemoved(StringeeVideoTrack stringeeVideoTrack) {

            }

            @Override
            public void onCallInfo(StringeeCall2 stringeeCall2, JSONObject jsonObject) {

            }

            @Override
            public void onTrackMediaStateChange(String s, StringeeVideoTrack.MediaType mediaType, boolean b) {

            }
        });

        // manage audio
        audioManager = new StringeeAudioManager(this);
        audioManager.start((audioDevice, set) -> {
            // start audio
        });

        audioManager.setSpeakerphoneOn(true);
        // khoi tao cuoc goi
        if(isInComingCall){
            // do chuong goi
            call.ringing(new StatusListener() {
                @Override
                public void onSuccess() {

                }
            });
        }else{
            call.makeCall(new StatusListener() {
                @Override
                public void onSuccess() {

                }
            });
        }
    }

//== report ===============================================================================
    AppCompatButton mBtnKhongThich;
    AppCompatButton mBtnQuayRoi;
    AppCompatButton mBtnBatHopPhap;
    AppCompatButton mBtnGianLan;
    AppCompatButton mBtnAnhGia;
    AppCompatButton mBtnBatNat;
    AppCompatButton mBtnViThanhNien;
    AppCompatButton mBtnKhac;
    EditText mEdtMoTa;
    AppCompatButton mBtnSend;
    AtomicBoolean isKhongThich = new AtomicBoolean(false);
    AtomicBoolean isQuayRoi = new AtomicBoolean(false);
    AtomicBoolean isBatHopPhap = new AtomicBoolean(false);
    AtomicBoolean isGianLan = new AtomicBoolean(false);
    AtomicBoolean isAnhGia = new AtomicBoolean(false);
    AtomicBoolean isBatNat = new AtomicBoolean(false);
    AtomicBoolean isViThanhNien = new AtomicBoolean(false);
    AtomicBoolean isKhac = new AtomicBoolean(false);
    AtomicBoolean isCanSend = new AtomicBoolean(false);
    String sMota = "";
    String sReport = "";
    private void showReport() {
        // when click report button
        View dialogSetting = getLayoutInflater().inflate(R.layout.bottom_sheet_report, null);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(dialogSetting);
        bottomSheetDialog.show();
        // extended bottom sheet
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from((View)dialogSetting.getParent());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        // set component
        mBtnKhongThich = dialogSetting.findViewById(R.id.btn_khong_thich);
        mBtnQuayRoi = dialogSetting.findViewById(R.id.btn_quay_roi);
        mBtnBatHopPhap = dialogSetting.findViewById(R.id.btn_bat_hop_phap);
        mBtnGianLan = dialogSetting.findViewById(R.id.btn_gian_lan);
        mBtnAnhGia = dialogSetting.findViewById(R.id.btn_anh_gia);
        mBtnBatNat = dialogSetting.findViewById(R.id.btn_bat_nat);
        mBtnViThanhNien = dialogSetting.findViewById(R.id.btn_vi_thanh_nien);
        mBtnKhac = dialogSetting.findViewById(R.id.btn_khac);
        mEdtMoTa = dialogSetting.findViewById(R.id.edt_mo_ta);
        mBtnSend = dialogSetting.findViewById(R.id.btn_send);

        isKhongThich = new AtomicBoolean(false);
        isQuayRoi = new AtomicBoolean(false);
        isBatHopPhap = new AtomicBoolean(false);
        isGianLan = new AtomicBoolean(false);
        isAnhGia = new AtomicBoolean(false);
        isBatNat = new AtomicBoolean(false);
        isViThanhNien = new AtomicBoolean(false);
        isKhac = new AtomicBoolean(false);
        isCanSend = new AtomicBoolean(false);

        mBtnKhongThich.setOnClickListener(v -> {
            isKhongThich.set(!isKhongThich.get());
            if(isKhongThich.get()) {
                setButtonColor(mBtnKhongThich, R.color.background_button_dark_2, R.color.white);
            } else {
                setButtonColor(mBtnKhongThich, R.color.background_button_2, R.color.text_primary);
            }
            checkCanSend();
        });
        mBtnQuayRoi.setOnClickListener(v -> {
            isQuayRoi.set(!isQuayRoi.get());
            if(isQuayRoi.get()) {
                setButtonColor(mBtnQuayRoi, R.color.background_button_dark_2, R.color.white);
            } else {
                setButtonColor(mBtnQuayRoi, R.color.background_button_2, R.color.text_primary);
            }
            checkCanSend();
        });
        mBtnBatHopPhap.setOnClickListener(v -> {
            isBatHopPhap.set(!isBatHopPhap.get());
            if(isBatHopPhap.get()) {
                setButtonColor(mBtnBatHopPhap, R.color.background_button_dark_2, R.color.white);
            } else {
                setButtonColor(mBtnBatHopPhap, R.color.background_button_2, R.color.text_primary);
            }
            checkCanSend();
        });
        mBtnGianLan.setOnClickListener(v -> {
            isGianLan.set(!isGianLan.get());
            if(isGianLan.get()) {
                setButtonColor(mBtnGianLan, R.color.background_button_dark_2, R.color.white);
            } else {
                setButtonColor(mBtnGianLan, R.color.background_button_2, R.color.text_primary);
            }
            checkCanSend();
        });
        mBtnAnhGia.setOnClickListener(v -> {
            isAnhGia.set(!isAnhGia.get());
            if(isAnhGia.get()) {
                setButtonColor(mBtnAnhGia, R.color.background_button_dark_2, R.color.white);
            } else {
                setButtonColor(mBtnAnhGia, R.color.background_button_2, R.color.text_primary);
            }
            checkCanSend();
        });
        mBtnBatNat.setOnClickListener(v -> {
            isBatNat.set(!isBatNat.get());
            if(isBatNat.get()) {
                setButtonColor(mBtnBatNat, R.color.background_button_dark_2, R.color.white);
            } else {
                setButtonColor(mBtnBatNat, R.color.background_button_2, R.color.text_primary);
            }
            checkCanSend();
        });
        mBtnViThanhNien.setOnClickListener(v -> {
            isViThanhNien.set(!isViThanhNien.get());
            if(isViThanhNien.get()) {
                setButtonColor(mBtnViThanhNien, R.color.background_button_dark_2, R.color.white);
            } else {
                setButtonColor(mBtnViThanhNien, R.color.background_button_2, R.color.text_primary);
            }
            checkCanSend();
        });
        // neu chua chon khac thi an di
        mEdtMoTa.setVisibility(View.GONE);
        mBtnKhac.setOnClickListener(v -> {
            isKhac.set(!isKhac.get());
            sMota = "";
            if(isKhac.get()) {
                setButtonColor(mBtnKhac, R.color.background_button_dark_2, R.color.white);
                mEdtMoTa.setVisibility(View.VISIBLE);
            } else {
                mEdtMoTa.setVisibility(View.GONE);
                mEdtMoTa.setText("");
                setButtonColor(mBtnKhac, R.color.background_button_2, R.color.text_primary);
            }
            checkCanSend();
        });
        mBtnSend.setOnClickListener(v -> {
            sMota = mEdtMoTa.getText().toString();
            setReportDescription();
            Toast.makeText(this, sReport, Toast.LENGTH_LONG).show();
            createNewReport();
            bottomSheetDialog.dismiss();
        });
    }

    private void setReportDescription() {
        sReport = "";
        if(isKhongThich.get()) sReport += "Không thích, ";
        if(isQuayRoi.get()) sReport += "Quấy rối tình dục, ";
        if(isBatHopPhap.get()) sReport += "Hoạt động bất hợp pháp, ";
        if(isGianLan.get()) sReport += "Gian lận, ";
        if(isAnhGia.get()) sReport += "Ảnh giả, ";
        if(isBatNat.get()) sReport += "Bắt nạt, ";
        if(isViThanhNien.get()) sReport += "Vị thành niên, ";
        if(isKhac.get()) sReport += "Khác, ";
        sReport += sMota;
    }

    private void checkCanSend() {
        isCanSend.set(isKhongThich.get() || isQuayRoi.get() || isBatHopPhap.get() || isGianLan.get() || isAnhGia.get() || isBatNat.get() || isViThanhNien.get() || isKhac.get());
        if(isCanSend.get()){
            mBtnSend.setClickable(true);
            setButtonColor(mBtnSend, R.color.primary_2, R.color.white);
        } else {
            mBtnSend.setClickable(false);
            setButtonColor(mBtnSend, R.color.text_tertiary, R.color.white);
        }
    }

    private void createNewReport() {
        Report report = new Report("1",
                ConnectRepo.getInstance().getUserRemote().getUid(),
                ConnectRepo.getInstance().getUserLocal().getUid(),
                new Date(),
                sReport);

        CollectionReference dbReport = FirebaseFirestore.getInstance().collection("REPORTS");
        dbReport.add(report).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                // update ID report
                report.setUid(documentReference.getId());
                dbReport.document(report.getUid()).set(report);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CallVideoActivity.this, "Fail to add report \n" + e, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setButtonColor(AppCompatButton btn, int colorButton, int colorText) {
        btn.setBackgroundTintList(this.getResources().getColorStateList(colorButton));
        btn.setTextColor(this.getResources().getColorStateList(colorText));
    }
}