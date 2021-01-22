package com.vip.raid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RadioGroup rgType;
    private RadioButton rdoIronHorse, rdoDark;
    private TextView txtTime;
    private LinearLayout[] layoutNamed = new LinearLayout[4];
    private TextView[] txtNamed = new TextView[4];
    private Button btnReset, btnAdd;

    private String[][] ironHorseTypes = {{"전방", "후방", "달콤한꿈+전방", "힐러"}, {"지휘통제실", "내부탱커", "A구역", "B구역", "C구역", "2층 딜러", "힐러"},
            {"3층 진단", "2층, 3층", "1층 초기화", "탱커", "힐러", "CC빌드(상태이상)", "경첩", "ABC", "저격수 처리", "경첩 파괴"}, {"모로조바탱", "키탱", "좌측 힐러", "우측 힐러", "좌측 전방 딜러", "우측 전방 딜러", "좌측 RPG 후방", "우측 RPG 후방"}};
    private String[][] darkTypes = {{"A", "B", "C", "D", "힐러", "드리블러", "기관포", "외부 딜러"}, {"A", "B"}, {"루시", "버디"}, {"1번 내부 (생존 전문가)", "1번 외부", "2번 내부 (생존 전문가)", "2번 외부", "3번 내부", "3번 외부", "4번 내부", "4번 외부"}};

    private String[] darkBoss = {"부머", "디지/리코챗 + 위젤", "루시 & 버디", "DDP-52 레이저백"};
    private String[] ironHorseBoss = {"그레이", "피서", "윌리엄스", "모로조바"};

    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;

    private AlertDialog alertDialog = null;
    private AlertDialog.Builder builder = null;
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rgType = findViewById(R.id.rgType);
        rdoIronHorse = findViewById(R.id.rdoIronHorse);
        rdoDark = findViewById(R.id.rdoDark);
        txtTime = findViewById(R.id.txtTime);
        btnReset = findViewById(R.id.btnReset);
        btnAdd = findViewById(R.id.btnAdd);

        for (int i = 0; i < layoutNamed.length; i++) {
            int resources = getResources().getIdentifier("layoutNamed"+(i+1), "id", getPackageName());
            layoutNamed[i] = findViewById(resources);
            resources = getResources().getIdentifier("txtNamed"+(i+1), "id", getPackageName());
            txtNamed[i] = findViewById(resources);
        }

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(calendar.YEAR);
        int month = calendar.get(calendar.MONTH)+1;
        int day = calendar.get(calendar.DATE);

        txtTime.setText(year+"년 "+month+"월 "+day+"일");
        mDatabase = FirebaseDatabase.getInstance();

        for (int i = 0; i < 4; i++) {
            txtNamed[i].setText("네임드 "+(i+1)+" - "+ironHorseBoss[i]);
            for (int j = 0; j < ironHorseTypes[i].length; j++) {
                View view = getLayoutInflater().inflate(R.layout.insertlayout, null);
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                param.bottomMargin = 5;
                view.setLayoutParams(param);

                final TextView txtType = view.findViewById(R.id.txtType);
                final TextView txtContent = view.findViewById(R.id.txtContent);
                txtType.setText(ironHorseTypes[i][j]+" : ");

                final int named_index = i;
                final int named_jndex = j;
                for (int index = 0; index < 8; index++) {
                    mReference = mDatabase.getReference("IronHorse/Member"+(index+1));
                    mReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String name = "nonergergerge";
                            boolean isFind = false;
                            for (DataSnapshot messageData : snapshot.getChildren()) {
                                if (messageData.getKey().toString().equals("name")) {
                                    name = messageData.getValue().toString();
                                } else if (messageData.getKey().toString().equals("Named"+(named_index+1))) {
                                    if (ironHorseTypes[named_index][named_jndex].equals(messageData.getValue().toString())) {
                                        isFind = true;
                                    }
                                }
                            }
                            if (isFind) {
                                if (txtContent.getText().toString().equals("없음")) {
                                    txtContent.setText(name);
                                } else {
                                    txtContent.setText(txtContent.getText().toString()+"\n"+name);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
                layoutNamed[i].addView(view);
            }
        }

        rgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rdoIronHorse) {
                    for (int i = 0; i < 4; i++) {
                        layoutNamed[i].removeAllViews();
                        /*TextView txtName = new TextView(getApplicationContext());
                        LinearLayout.LayoutParams textview_param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        textview_param.bottomMargin = 5;
                        txtName.setLayoutParams(textview_param);
                        txtName.setText("네임드 "+(i+1)+" - "+ironHorseBoss[i]);
                        txtName.setTextColor(Color.parseColor("#f0f0f0"));
                        txtName.setTextSize(12);*/
                        txtNamed[i].setText("네임드 "+(i+1)+" - "+ironHorseBoss[i]);

                        for (int j = 0; j < ironHorseTypes[i].length; j++) {
                            View view = getLayoutInflater().inflate(R.layout.insertlayout, null);
                            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            param.bottomMargin = 5;
                            view.setLayoutParams(param);

                            final TextView txtType = view.findViewById(R.id.txtType);
                            final TextView txtContent = view.findViewById(R.id.txtContent);
                            txtType.setText(ironHorseTypes[i][j]+" : ");

                            final int named_index = i;
                            final int named_jndex = j;
                            for (int index = 0; index < 8; index++) {
                                mReference = mDatabase.getReference("IronHorse/Member"+(index+1));
                                mReference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String name = "nonergergerge";
                                        boolean isFind = false;
                                        for (DataSnapshot messageData : snapshot.getChildren()) {
                                            if (messageData.getKey().toString().equals("name")) {
                                                name = messageData.getValue().toString();
                                            } else if (messageData.getKey().toString().equals("Named"+(named_index+1))) {
                                                if (ironHorseTypes[named_index][named_jndex].equals(messageData.getValue().toString())) {
                                                    isFind = true;
                                                }
                                            }
                                        }
                                        if (isFind) {
                                            if (txtContent.getText().toString().equals("없음")) {
                                                txtContent.setText(name);
                                            } else {
                                                txtContent.setText(txtContent.getText().toString()+"\n"+name);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                            layoutNamed[i].addView(view);
                        }
                    }
                } else {
                    for (int i = 0; i < 4; i++) {
                        layoutNamed[i].removeAllViews();
                        /*TextView txtName = new TextView(getApplicationContext());
                        LinearLayout.LayoutParams textview_param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        textview_param.bottomMargin = 5;
                        txtName.setLayoutParams(textview_param);
                        txtName.setText("네임드 "+(i+1)+" - "+darkBoss[i]);
                        txtName.setTextColor(Color.parseColor("#f0f0f0"));
                        txtName.setTextSize(12);*/
                        txtNamed[i].setText("네임드 "+(i+1)+" - "+darkBoss[i]);

                        for (int j = 0; j < darkTypes[i].length; j++) {
                            View view = getLayoutInflater().inflate(R.layout.insertlayout, null);
                            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            param.bottomMargin = 5;
                            view.setLayoutParams(param);

                            final TextView txtType = view.findViewById(R.id.txtType);
                            final TextView txtContent = view.findViewById(R.id.txtContent);
                            txtType.setText(darkTypes[i][j]+" : ");

                            final int named_index = i;
                            final int named_jndex = j;
                            for (int index = 0; index < 8; index++) {
                                mReference = mDatabase.getReference("Dark/Member"+(index+1));
                                mReference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String name = "nonergergerge";
                                        boolean isFind = false;
                                        for (DataSnapshot messageData : snapshot.getChildren()) {
                                            if (messageData.getKey().toString().equals("name")) {
                                                name = messageData.getValue().toString();
                                            } else if (messageData.getKey().toString().equals("Named"+(named_index+1))) {
                                                if (darkTypes[named_index][named_jndex].equals(messageData.getValue().toString())) {
                                                    isFind = true;
                                                }
                                            }
                                        }
                                        if (isFind) {
                                            if (txtContent.getText().toString().equals("없음")) {
                                                txtContent.setText(name);
                                            } else {
                                                txtContent.setText(txtContent.getText().toString()+"\n"+name);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                            layoutNamed[i].addView(view);
                        }
                    }
                }
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(getApplicationContext(), AddActivity.class);
                startActivity(intent);
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view = getLayoutInflater().inflate(R.layout.resetlayout, null);

                final EditText edtPassword = view.findViewById(R.id.edtPassword);
                final Button btnProcess = view.findViewById(R.id.btnProcess);

                btnProcess.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (edtPassword.getText().toString().equals("3287")) {
                            Map<String, Object> taskMap = new HashMap<String, Object>();
                            taskMap.put("name", "none");
                            for (int j = 0; j < 4; j++) taskMap.put("Named"+(j+1), "none");
                            for (int i = 0; i < 8; i++) {
                                mReference = mDatabase.getReference("IronHorse/Member"+(i+1));
                                mReference.updateChildren(taskMap);
                                mReference = mDatabase.getReference("Dark/Member"+(i+1));
                                mReference.updateChildren(taskMap);
                            }
                            Toast.makeText(getApplicationContext(), "모두 초기화되었습니다.", Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                            onResume();
                        } else {
                            Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder = new AlertDialog.Builder(MainActivity.this);
                builder.setView(view);

                alertDialog =builder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rdoIronHorse.isChecked()) {
            for (int i = 0; i < 4; i++) {
                layoutNamed[i].removeAllViews();
                        /*TextView txtName = new TextView(getApplicationContext());
                        LinearLayout.LayoutParams textview_param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        textview_param.bottomMargin = 5;
                        txtName.setLayoutParams(textview_param);
                        txtName.setText("네임드 "+(i+1)+" - "+ironHorseBoss[i]);
                        txtName.setTextColor(Color.parseColor("#f0f0f0"));
                        txtName.setTextSize(12);*/
                txtNamed[i].setText("네임드 "+(i+1)+" - "+ironHorseBoss[i]);

                for (int j = 0; j < ironHorseTypes[i].length; j++) {
                    View view = getLayoutInflater().inflate(R.layout.insertlayout, null);
                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    param.bottomMargin = 5;
                    view.setLayoutParams(param);

                    final TextView txtType = view.findViewById(R.id.txtType);
                    final TextView txtContent = view.findViewById(R.id.txtContent);
                    txtType.setText(ironHorseTypes[i][j]+" : ");

                    final int named_index = i;
                    final int named_jndex = j;
                    for (int index = 0; index < 8; index++) {
                        mReference = mDatabase.getReference("IronHorse/Member"+(index+1));
                        mReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String name = "nonergergerge";
                                boolean isFind = false;
                                for (DataSnapshot messageData : snapshot.getChildren()) {
                                    if (messageData.getKey().toString().equals("name")) {
                                        name = messageData.getValue().toString();
                                    } else if (messageData.getKey().toString().equals("Named"+(named_index+1))) {
                                        if (ironHorseTypes[named_index][named_jndex].equals(messageData.getValue().toString())) {
                                            isFind = true;
                                        }
                                    }
                                }
                                if (isFind) {
                                    if (txtContent.getText().toString().equals("없음")) {
                                        txtContent.setText(name);
                                    } else {
                                        txtContent.setText(txtContent.getText().toString()+"\n"+name);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                    layoutNamed[i].addView(view);
                }
            }
        } else {
            for (int i = 0; i < 4; i++) {
                layoutNamed[i].removeAllViews();
                        /*TextView txtName = new TextView(getApplicationContext());
                        LinearLayout.LayoutParams textview_param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        textview_param.bottomMargin = 5;
                        txtName.setLayoutParams(textview_param);
                        txtName.setText("네임드 "+(i+1)+" - "+darkBoss[i]);
                        txtName.setTextColor(Color.parseColor("#f0f0f0"));
                        txtName.setTextSize(12);*/
                txtNamed[i].setText("네임드 "+(i+1)+" - "+darkBoss[i]);

                for (int j = 0; j < darkTypes[i].length; j++) {
                    View view = getLayoutInflater().inflate(R.layout.insertlayout, null);
                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    param.bottomMargin = 5;
                    view.setLayoutParams(param);

                    final TextView txtType = view.findViewById(R.id.txtType);
                    final TextView txtContent = view.findViewById(R.id.txtContent);
                    txtType.setText(darkTypes[i][j]+" : ");

                    final int named_index = i;
                    final int named_jndex = j;
                    for (int index = 0; index < 8; index++) {
                        mReference = mDatabase.getReference("Dark/Member"+(index+1));
                        mReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String name = "nonergergerge";
                                boolean isFind = false;
                                for (DataSnapshot messageData : snapshot.getChildren()) {
                                    if (messageData.getKey().toString().equals("name")) {
                                        name = messageData.getValue().toString();
                                    } else if (messageData.getKey().toString().equals("Named"+(named_index+1))) {
                                        if (darkTypes[named_index][named_jndex].equals(messageData.getValue().toString())) {
                                            isFind = true;
                                        }
                                    }
                                }
                                if (isFind) {
                                    if (txtContent.getText().toString().equals("없음")) {
                                        txtContent.setText(name);
                                    } else {
                                        txtContent.setText(txtContent.getText().toString()+"\n"+name);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                    layoutNamed[i].addView(view);
                }
            }
        }
    }
}
