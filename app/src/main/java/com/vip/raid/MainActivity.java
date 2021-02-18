package com.vip.raid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final int TYPE_WIFI = 1;
    public static final int TYPE_MOBILE = 2;
    public static final int TYPE_NOT_CONNECTED = 3;

    private RadioGroup rgType;
    private RadioButton rdoIronHorse, rdoDark;
    private TextView txtTime, txtPeople, txtCommander, txtVersion, txtConnect;
    private LinearLayout[] layoutNamed = new LinearLayout[4];
    private TextView[] txtNamed = new TextView[4];
    private Button btnReset, btnAdd;

    private String[][] ironHorseTypes = {{"전방", "후방", "달콤한꿈+전방", "힐러"}, {"지휘통제실", "내부탱커", "A구역", "B구역", "C구역", "2층 딜러", "힐러"},
            {"3층 진단", "2층, 3층 + 경첩", "1층 초기화", "탱커", "힐러", "CC빌드(상태이상)", "ABC", "저격수 처리", "경첩 파괴", "잡몹 처리"}, {"모로조바탱", "키탱", "좌측 힐러", "우측 힐러", "좌측 전방 딜러", "우측 전방 딜러", "좌측 RPG 후방", "우측 RPG 후방", "잡몹 처리"}};
    private String[][] darkTypes = {{"A", "B", "C", "D", "힐러", "드리블러", "기관포", "외부 딜러"}, {"A", "B"}, {"루시", "버디"}, {"1번 내부 (생존 전문가)", "1번 외부", "2번 내부 (생존 전문가)", "2번 외부", "3번 내부", "3번 외부", "4번 내부", "4번 외부"}};

    private String[] darkBoss = {"부머", "디지/리코챗 + 위젤", "루시 & 버디", "DDP-52 레이저백"};
    private String[] ironHorseBoss = {"그레이", "피서", "윌리엄스", "모로조바"};

    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;

    private AlertDialog alertDialog = null;
    private AlertDialog.Builder builder = null;
    private View view;

    private int people = 0;
    private boolean isNull = true;

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
        txtPeople = findViewById(R.id.txtPeople);
        txtCommander = findViewById(R.id.txtCommander);
        txtVersion = findViewById(R.id.txtVersion);
        txtConnect = findViewById(R.id.txtConnect);

        txtVersion.setText("Version "+getAppVersionName());

        if (getConnectivityStatus(getApplicationContext()) == TYPE_MOBILE) {
            txtConnect.setText("LTE 또는 3G");
            txtConnect.setTextColor(Color.YELLOW);
        } else if (getConnectivityStatus(getApplicationContext()) == TYPE_WIFI) {
            txtConnect.setText("WI-FI");
            txtConnect.setTextColor(Color.GREEN);
        } else {
            txtConnect.setText("연결되어 있지 않음");
            txtConnect.setTextColor(Color.parseColor("#FF4444"));
            Toast.makeText(getApplicationContext(), "인터넷이 연결되어 있지 않습니다. 연결 상태를 확인해주십시오.", Toast.LENGTH_SHORT).show();
        }

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
        txtCommander.setText("없음");
        mDatabase = FirebaseDatabase.getInstance();

        mReference = mDatabase.getReference();
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!getAppVersionName().equals(snapshot.child("Version").getValue())) {
                    View view = getLayoutInflater().inflate(R.layout.deletelayout, null);

                    TextView txtContent = view.findViewById(R.id.txtContent);
                    Button btnCancel = view.findViewById(R.id.btnCancel);
                    Button btnDelete = view.findViewById(R.id.btnDelete);

                    txtContent.setText("현재 버전보다 상위 버전이 존재합니다. 업데이트가 필요합니다.\n"+"현재 버전 : "+getAppVersionName()+"\n최신 버전 : "+snapshot.child("Version").getValue());
                    btnCancel.setText("종료");
                    btnDelete.setText("다운로드");

                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });

                    btnDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.com/channels/743235040132988949/805931040786088006/811781268773142548"));
                            startActivity(intent);
                        }
                    });

                    builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setView(view);

                    alertDialog =builder.create();
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        for (int index = 0; index < 8; index++) {
            mReference = mDatabase.getReference("IronHorse/Member"+(index+1));
            mReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean isFind = false;
                    for (DataSnapshot messageData : snapshot.getChildren()) {
                        if (messageData.getKey().toString().equals("Commander") && messageData.getValue().toString().equals("true")) isFind = true;
                        else if (isFind && messageData.getKey().toString().equals("name")) {
                            txtCommander.setText(messageData.getValue().toString());
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        for (int i = 0; i < 4; i++) {
            txtNamed[i].setText("네임드 "+(i+1)+" - "+ironHorseBoss[i]);
            for (int j = 0; j < ironHorseTypes[i].length; j++) {
                final View view = getLayoutInflater().inflate(R.layout.insertlayout, null);
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
                                if (txtContent.getText().toString().equals("-")) {
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
                    checkPeople("IronHorse");
                    rdoIronHorse.setTextColor(Color.parseColor("#FE6E0E"));
                    rdoDark.setTextColor(Color.parseColor("#AAAAAA"));
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
                            final View view = getLayoutInflater().inflate(R.layout.insertlayout, null);
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
                                            if (txtContent.getText().toString().equals("-")) {
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
                    checkPeople("Dark");
                    rdoDark.setTextColor(Color.parseColor("#FE6E0E"));
                    rdoIronHorse.setTextColor(Color.parseColor("#AAAAAA"));
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
                            final View view = getLayoutInflater().inflate(R.layout.insertlayout, null);
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
                                            if (txtContent.getText().toString().equals("-")) {
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
                txtCommander.setText("없음");
                for (int index = 0; index < 8; index++) {
                    if (checkedId == R.id.rdoIronHorse) mReference = mDatabase.getReference("IronHorse/Member"+(index+1));
                    else mReference = mDatabase.getReference("Dark/Member"+(index+1));
                    mReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean isFind = false;
                            for (DataSnapshot messageData : snapshot.getChildren()) {
                                if (messageData.getKey().toString().equals("Commander") && messageData.getValue().toString().equals("true")) isFind = true;
                                else if (isFind && messageData.getKey().toString().equals("name")) {
                                    txtCommander.setText(messageData.getValue().toString());
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddActivity.class);
                startActivity(intent);
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view = getLayoutInflater().inflate(R.layout.resetlayout, null);

                final EditText edtPassword = view.findViewById(R.id.edtPassword);
                final Button btnProcess = view.findViewById(R.id.btnProcess);
                final Button btnCancel = view.findViewById(R.id.btnCancel);

                btnProcess.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (edtPassword.getText().toString().equals("3287")) {
                            Map<String, Object> taskMap = new HashMap<String, Object>();
                            taskMap.put("name", "none");
                            taskMap.put("Commander", "false");
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

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
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
        isNull = true;
        txtCommander.setText("없음");
        for (int index = 0; index < 8; index++) {
            if (rdoIronHorse.isChecked()) mReference = mDatabase.getReference("IronHorse/Member"+(index+1));
            else mReference = mDatabase.getReference("Dark/Member"+(index+1));
            mReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean isFind = false;
                    for (DataSnapshot messageData : snapshot.getChildren()) {
                        if (messageData.getKey().toString().equals("Commander") && messageData.getValue().toString().equals("true")) {
                            isFind = true;
                            isNull = false;
                        } else if (isFind && messageData.getKey().toString().equals("name")) {
                            txtCommander.setText(messageData.getValue().toString());
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
        if (isNull) txtCommander.setText("없음");
        if (rdoIronHorse.isChecked()) {
            checkPeople("IronHorse");
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
                    final View view = getLayoutInflater().inflate(R.layout.insertlayout, null);
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
                                    if (txtContent.getText().toString().equals("-")) {
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
            checkPeople("Dark");
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
                    final View view = getLayoutInflater().inflate(R.layout.insertlayout, null);
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
                                    if (txtContent.getText().toString().equals("-")) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_btn1:
                view = getLayoutInflater().inflate(R.layout.memberlayout, null);
                final ListView listView = view.findViewById(R.id.listView);
                final TextView txtInfo = view.findViewById(R.id.txtInfo);

                final ArrayList<String> list = new ArrayList<String>();
                String type = "";
                if (rdoIronHorse.isChecked()) type = "IronHorse";
                else type = "Dark";
                for (int index = 0; index < 8; index++) {
                    mReference = mDatabase.getReference(type+"/Member"+(index+1));
                    mReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot messageData : snapshot.getChildren()) {
                                if (messageData.getKey().toString().equals("name") && !messageData.getValue().toString().equals("none")) {
                                    list.add(messageData.getValue().toString());
                                    if (!list.isEmpty()) {
                                        txtInfo.setVisibility(View.GONE);
                                        listView.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, list) {
                    @NonNull
                    @Override
                    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);

                        final TextView tv = (TextView) view.findViewById(android.R.id.text1);

                        if (rdoIronHorse.isChecked()) mReference = mDatabase.getReference("IronHorse");
                        else mReference = mDatabase.getReference("Dark");
                        mReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (int index = 0; index < 8; index++) {
                                    if (snapshot.child("Member"+(index+1)).child("Commander").getValue().equals("true") && snapshot.child("Member"+(index+1)).child("name").getValue().equals(list.get(position))) {
                                        tv.setTextColor(Color.parseColor("#FF4444"));
                                        break;
                                    } else tv.setTextColor(Color.WHITE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        /*for (int index = 0; index < 8; index++) {
                            if (rdoIronHorse.isChecked()) mReference = mDatabase.getReference("IronHorse/Member"+(index+1));
                            else mReference = mDatabase.getReference("Dark/Member"+(index+1));
                            mReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    boolean isCommander = false;
                                    for (DataSnapshot messageData : snapshot.getChildren()) {
                                        if (messageData.getKey().toString().equals("Commander") && messageData.getValue().toString().equals("true")) isCommander = true;
                                        if (messageData.getKey().toString().equals("name") && messageData.getValue().toString().equals(list.get(position)) && isCommander) tv.setText(tv.getText().toString()+" (공대장)");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }*/

                        tv.setTextColor(Color.WHITE);
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                                LinearLayout.LayoutParams.FILL_PARENT ); //텍스트 뷰의 크기를 부모의 크기에 맞춘후
                        tv.setLayoutParams(layoutParams);
                        tv.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);

                        return view;
                    }
                };
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        String location;
                        String[] named = new String[4];
                        if (rdoIronHorse.isChecked()) location = "IronHorse";
                        else location = "Dark";
                        mReference = mDatabase.getReference(location);
                        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String str = "";
                                for (DataSnapshot messageData : snapshot.getChildren()) {
                                    if (list.get(position).equals(messageData.child("name").getValue())) {
                                        str += messageData.child("name").getValue();
                                        if (messageData.child("Commander").getValue().equals("true")) str += " (공대장)";
                                        str += "\n-----------------------------------------";
                                        for (int i = 1; i <= 4; i++) str += "\n네임드"+i+" : "+messageData.child("Named"+i).getValue();
                                        break;
                                    }
                                }
                                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });

                builder = new AlertDialog.Builder(this);
                builder.setView(view);

                alertDialog =builder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();

                return true;
            case R.id.action_btn4:
                Intent intent = new Intent(getApplicationContext(), MembersActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_btn5:
                onResume();
                Toast.makeText(getApplicationContext(), "새로 고침했습니다.", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkPeople(String type) {
        for (int index = 0; index < 8; index++) {
            mReference = mDatabase.getReference(type+"/Member"+(index+1));
            final int position_index = index;
            people = 0;
            txtPeople.setText(Integer.toString(people));
            mReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot messageData : snapshot.getChildren()) {
                        if (messageData.getKey().toString().equals("name") && !messageData.getValue().toString().equals("none")) {
                            people++;
                            txtPeople.setText(Integer.toString(people));
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }

    public String getAppVersionName(){
        PackageInfo packageInfo = null;         //패키지에 대한 전반적인 정보

        //PackageInfo 초기화
        try{
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
            return "";
        }

        return packageInfo.versionName;
    }

    public static int getConnectivityStatus(Context context){ //해당 context의 서비스를 사용하기위해서 context객체를 받는다.
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if(networkInfo != null){
            int type = networkInfo.getType();
            if(type == ConnectivityManager.TYPE_MOBILE){//쓰리지나 LTE로 연결된것(모바일을 뜻한다.)
                return TYPE_MOBILE;
            }else if(type == ConnectivityManager.TYPE_WIFI){//와이파이 연결된것
                return TYPE_WIFI;
            }
        }
        return TYPE_NOT_CONNECTED;  //연결이 되지않은 상태
    }
}
