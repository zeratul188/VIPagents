package com.vip.raid;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddActivity extends AppCompatActivity {

    private EditText edtName;
    private RadioGroup rgType;
    private RadioButton rdoIronHorse, rdoDark;
    private Button[] btnNamed = new Button[4];
    private Button btnAdd, btnRemove, btnSave, btnInput;
    private TextView[] txtNamed = new TextView[4];
    private CheckBox chkCommander;

    private String[][] ironHorseTypes = {{"전방", "후방", "달콤한꿈+전방", "힐러"}, {"지휘통제실", "내부탱커", "A구역", "B구역", "C구역", "2층 딜러", "힐러"},
            {"3층 진단", "2층, 3층 + 경첩", "1층 초기화", "탱커", "힐러", "CC빌드(상태이상)", "ABC", "저격수 처리", "경첩 파괴", "잡몹 처리"}, {"모로조바탱", "키탱", "좌측 힐러", "우측 힐러", "좌측 전방 딜러", "우측 전방 딜러", "좌측 RPG 후방", "우측 RPG 후방", "잡몹 처리"}};
    private String[][] darkTypes = {{"A", "B", "C", "D", "힐러", "드리블러", "기관포", "외부 딜러"}, {"A", "B"}, {"루시", "버디"}, {"1번 내부 (생존 전문가)", "1번 외부", "2번 내부 (생존 전문가)", "2번 외부", "3번 내부", "3번 외부", "4번 내부", "4번 외부"}};

    private int[][] ironHorseMax = {{8, 1, 3, 1}, {1, 1, 2, 2, 2, 1, 1}, {1, 1, 1, 1, 1, 1, 1, 8, 2, 8}, {1, 1, 1, 1, 1, 1, 1, 1, 8}};
    private int[][] darkMax = {{1, 1, 1, 1, 1, 2, 2, 8}, {8, 8}, {8, 8}, {1, 1, 1, 1, 1, 1, 1, 1}};

    private String[] darkBoss = {"부머", "디지/리코챗 + 위젤", "루시 & 버디", "DDP-52 레이저백"};
    private String[] ironHorseBoss = {"그레이", "피서", "윌리엄스", "모로조바"};

    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;

    private ArrayList<String> ironHorseNames, darkNames;

    private boolean isIronHorse = true, isFull = true, isCommander = false;
    private int position = 0, max = 8, cnt = 0;

    private AlertDialog alertDialog = null;
    private AlertDialog.Builder builder = null;
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addlayout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("역할 저장/수정");

        edtName = findViewById(R.id.edtName);
        rgType = findViewById(R.id.rgType);
        rdoIronHorse = findViewById(R.id.rdoIronHorse);
        rdoDark = findViewById(R.id.rdoDark);
        btnAdd = findViewById(R.id.btnAdd);
        btnRemove = findViewById(R.id.btnRemove);
        btnSave = findViewById(R.id.btnSave);
        btnInput = findViewById(R.id.btnInput);
        chkCommander = findViewById(R.id.chkCommander);

        for (int i = 0; i < btnNamed.length; i++) {
            int resources = getResources().getIdentifier("btnNamed"+(i+1), "id", getPackageName());
            btnNamed[i] = findViewById(resources);
            resources = getResources().getIdentifier("txtNamed"+(i+1), "id", getPackageName());
            txtNamed[i] = findViewById(resources);
        }

        ironHorseNames = new ArrayList<String>();
        darkNames = new ArrayList<String>();

        mDatabase = FirebaseDatabase.getInstance();
        checkFull("IronHorse");
        rgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                for (int i = 0; i < btnNamed.length; i++) {
                    btnNamed[i].setText("없음");
                }
                if (checkedId == R.id.rdoIronHorse) {
                    checkFull("IronHorse");
                    isIronHorse = true;
                    for (int i = 0; i < txtNamed.length; i++) {
                        txtNamed[i].setText("네임드"+(i+1)+" - "+ironHorseBoss[i]);
                    }
                } else {
                    checkFull("Dark");
                    isIronHorse = false;
                    for (int i = 0; i < txtNamed.length; i++) {
                        txtNamed[i].setText("네임드"+(i+1)+" - "+darkBoss[i]);
                    }
                }
                isCommander = false;
                chkCommander.setChecked(false);
                for (int index = 0; index < 8; index++) {
                    if (isIronHorse) mReference = mDatabase.getReference("IronHorse/Member"+(index+1));
                    else mReference = mDatabase.getReference("Dark/Member"+(index+1));
                    mReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot messageData : snapshot.getChildren()) {
                                if (messageData.getKey().toString().equals("Commander") && Boolean.parseBoolean(messageData.getValue().toString())) {
                                    isCommander = true;
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

        for (int i = 0; i < txtNamed.length; i++) {
            txtNamed[i].setText(txtNamed[i].getText().toString()+ironHorseBoss[i]);
        }

        for (int index = 0; index < 8; index++) {
            if (isIronHorse) mReference = mDatabase.getReference("IronHorse/Member"+(index+1));
            else mReference = mDatabase.getReference("Dark/Member"+(index+1));
            mReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot messageData : snapshot.getChildren()) {
                        if (messageData.getKey().toString().equals("Commander") && Boolean.parseBoolean(messageData.getValue().toString())) {
                            isCommander = true;
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        for (int i = 0; i < btnNamed.length; i++) {
            final int index_i = i;
            btnNamed[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    view = getLayoutInflater().inflate(R.layout.dialoglayout, null);
                    final ListView listView = view.findViewById(R.id.listView);

                    final ArrayList<String> list = new ArrayList<String>();
                    if (isIronHorse) {
                        for (int i = 0; i < ironHorseTypes[index_i].length; i++) {
                            list.add(ironHorseTypes[index_i][i]);
                        }
                    } else {
                        for (int i = 0; i < darkTypes[index_i].length; i++) {
                            list.add(darkTypes[index_i][i]);
                        }
                    }
                    list.add("없음");

                    final Map<String, Integer> taskMap = new HashMap<String, Integer>();
                    for (int i = 0; i < list.size(); i++) {
                        taskMap.put(list.get(i), 0);
                    }

                    ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, list) {
                        @NonNull
                        @Override
                        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);

                            final TextView tv = (TextView) view.findViewById(android.R.id.text1);
                            tv.setTextColor(Color.WHITE);

                            final int final_position = position;
                            if (isIronHorse) {
                                for (int index = 0; index < 8; index++) {
                                    mReference = mDatabase.getReference("IronHorse/Member"+(index+1));
                                    mReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String str = list.get(final_position);
                                            for (int i = 0; i < ironHorseTypes[index_i].length; i++) {
                                                if (str.equals(ironHorseTypes[index_i][i])) {
                                                    max = ironHorseMax[index_i][i];
                                                    break;
                                                } else max = 8;
                                            }
                                            for (DataSnapshot messageData : snapshot.getChildren()) {
                                                if (messageData.getKey().toString().equals("Named"+(index_i+1)) && messageData.getValue().toString().equals(str)) {
                                                    taskMap.put(str, taskMap.get(str)+1);
                                                    if (taskMap.get(str)/8 >= max) {
                                                        tv.setVisibility(View.GONE);
                                                    }
                                                    tv.setText(str+" ("+(taskMap.get(str)/8)+")");
                                                    break;
                                                }
                                                if (!str.equals("없음")) tv.setText(str+" ("+(taskMap.get(str)/8)+")");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            } else {
                                for (int index = 0; index < 8; index++) {
                                    mReference = mDatabase.getReference("Dark/Member"+(index+1));
                                    mReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String str = list.get(final_position);
                                            for (int i = 0; i < darkTypes[index_i].length; i++) {
                                                if (str.equals(darkTypes[index_i][i])) {
                                                    max = darkMax[index_i][i];
                                                    break;
                                                } else max = 8;
                                            }
                                            for (DataSnapshot messageData : snapshot.getChildren()) {
                                                if (messageData.getKey().toString().equals("Named"+(index_i+1)) && messageData.getValue().toString().equals(str)) {
                                                    taskMap.put(str, taskMap.get(str)+1);
                                                    if (taskMap.get(str)/8 >= max) {
                                                        tv.setVisibility(View.GONE);
                                                    }
                                                    tv.setText(str+" ("+(taskMap.get(str)/8)+")");
                                                    break;
                                                }
                                                if (!str.equals("없음")) tv.setText(str+" ("+(taskMap.get(str)/8)+")");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            }

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
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            btnNamed[index_i].setText(list.get(position));
                            alertDialog.dismiss();
                        }
                    });

                    builder = new AlertDialog.Builder(AddActivity.this);
                    builder.setView(view);

                    alertDialog =builder.create();
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertDialog.show();
                }
            });

            for (int index = 0; index < 8; index++) {
                mReference = mDatabase.getReference("IronHorse/Member"+(index+1));
                mReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot messageData : snapshot.getChildren()) {
                            if (messageData.getKey().toString().equals("name")) {
                                ironHorseNames.add(messageData.getValue().toString());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            for (int index = 0; index < 8; index++) {
                mReference = mDatabase.getReference("Dark/Member"+(index+1));
                mReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot messageData : snapshot.getChildren()) {
                            if (messageData.getKey().toString().equals("name")) {
                                darkNames.add(messageData.getValue().toString());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (edtName.getText().toString().equals("")) {
                        Toast.makeText(AddActivity.this, "닉네임이 비어있습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean isHave = false;
                    for (int i = 0; i < ironHorseNames.size(); i++) {
                        if (rdoIronHorse.isChecked()) {
                            if (ironHorseNames.get(i).equals(edtName.getText().toString())) {
                                mReference = mDatabase.getReference("IronHorse/Member"+(i+1));
                                Map<String, Object> taskMap = new HashMap<String, Object>();
                                for (int index = 0; index < btnNamed.length; index++) taskMap.put("Named"+(index+1), btnNamed[index].getText().toString());
                                if (chkCommander.isChecked()) {
                                    if (isCommander) {
                                        toast("이미 공대장이 존재합니다.", false);
                                        return;
                                    }
                                    taskMap.put("Commander", "true");
                                } else taskMap.put("Commander", "false");
                                mReference.updateChildren(taskMap);
                                //Toast.makeText(AddActivity.this, "정보를 수정하였습니다.", Toast.LENGTH_SHORT).show();
                                isHave = true;
                                break;
                            }
                        } else {
                            if (darkNames.get(i).equals(edtName.getText().toString())) {
                                mReference = mDatabase.getReference("Dark/Member"+(i+1));
                                Map<String, Object> taskMap = new HashMap<String, Object>();
                                for (int index = 0; index < btnNamed.length; index++) taskMap.put("Named"+(index+1), btnNamed[index].getText().toString());
                                if (chkCommander.isChecked()) {
                                    if (isCommander) {
                                        toast("이미 공대장이 존재합니다.", false);
                                        return;
                                    }
                                    taskMap.put("Commander", "true");
                                } else taskMap.put("Commander", "false");
                                mReference.updateChildren(taskMap);
                                Toast.makeText(AddActivity.this, "정보를 수정하였습니다.", Toast.LENGTH_SHORT).show();
                                isHave = true;
                                break;
                            }
                        }
                    }
                    if (!isHave) {
                        if (isIronHorse) {
                            if (isFull) {
                                toast("참가 인원 수가 초과되었습니다.", false);
                                return;
                            }
                            mReference = mDatabase.getReference("IronHorse/Member"+position);
                        } else {
                            if (isFull) {
                                toast("참가 인원 수가 초과되었습니다.", false);
                                return;
                            }
                            mReference = mDatabase.getReference("Dark/Member"+position);
                        }
                        Map<String, Object> taskMap = new HashMap<String, Object>();
                        taskMap.put("name", edtName.getText().toString());
                        for (int i = 0; i < btnNamed.length; i++) taskMap.put("Named"+(i+1), btnNamed[i].getText().toString());
                        if (chkCommander.isChecked()) {
                            if (isCommander) {
                                toast("이미 공대장이 존재합니다.", false);
                                return;
                            }
                            taskMap.put("Commander", "true");
                        } else taskMap.put("Commander", "false");
                        mReference.updateChildren(taskMap);
                        Toast.makeText(AddActivity.this, "레이드 등록 하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                }
            });

            btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i = 0; i < ironHorseNames.size(); i++) {
                        if (rdoIronHorse.isChecked()) {
                            if (ironHorseNames.get(i).equals(edtName.getText().toString())) {
                                mReference = mDatabase.getReference("IronHorse/Member"+(i+1));
                                Map<String, Object> taskMap = new HashMap<String, Object>();
                                taskMap.put("name", "none");
                                taskMap.put("Commander", "false");
                                for (int t = 0; t < btnNamed.length; t++) taskMap.put("Named"+(t+1), "none");
                                mReference.updateChildren(taskMap);
                                Toast.makeText(AddActivity.this, "정보를 삭제하였습니다.", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        } else {
                            if (darkNames.get(i).equals(edtName.getText().toString())) {
                                mReference = mDatabase.getReference("Dark/Member"+(i+1));
                                Map<String, Object> taskMap = new HashMap<String, Object>();
                                taskMap.put("name", "none");
                                taskMap.put("Commander", "false");
                                for (int t = 0; t < btnNamed.length; t++) taskMap.put("Named"+(t+1), "none");
                                mReference.updateChildren(taskMap);
                                Toast.makeText(AddActivity.this, "정보를 삭제하였습니다.", Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    }
                    finish();
                }
            });
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edtName.getText().toString().equals("")) {
                    saveName(edtName.getText().toString());
                    toast("아이디를 저장하였습니다.", false);
                } else toast("아이디를 입력하십시오.", false);
            }
        });

        btnInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!loadName().equals("/*null*/")) {
                    edtName.setText(loadName());
                    toast("아이디를 불러왔습니다.", false);
                } else toast("저장된 아이디가 없습니다.", false);
            }
        });

    }

    /*private void saveLoadoutData() {
        if (hasPermissions()) {
            String databaseName = loadoutDBAdapter.getDatabaseName();
            String backupDirectoryName = "Division2Databases";
            try {
                File sd = Environment.getExternalStorageDirectory();
                File data = Environment.getDataDirectory();
                if (sd.canWrite()) {
                    File backupDir = new File(sd, backupDirectoryName);
                    if (!backupDir.exists()) backupDir.mkdir();
                    String currentDBPath = "//data//" + getPackageName()+ "//databases//" + databaseName;
                    String backupDBPath = "loadout_savefile";
                    File currentDB = new File(data, currentDBPath);
                    File backupDB = new File(sd, backupDirectoryName+"/"+backupDBPath);

                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();

                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                } else {
                    toast("권한 오류", false);
                }
            } catch (Exception e) {
                toast("Import Failed!!", false);
                e.printStackTrace();
            }
        } else {
            requestPerms();
        }
    }

    private void loadLoadout() {
        if (hasPermissions()) {
            String databaseName = loadoutDBAdapter.getDatabaseName();
            String backupDirectoryName = "Division2Databases";
            try {
                File sd = Environment.getExternalStorageDirectory();
                File data = Environment.getDataDirectory();
                if (sd.canWrite()) {
                    String currentDBPath = "//data//" + getPackageName()+ "//databases//" + databaseName;
                    String backupDBPath = "loadout_savefile";
                    File backupDB = new File(data, currentDBPath);
                    File currentDB = new File(sd, backupDirectoryName+"/"+backupDBPath);

                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();

                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                } else {
                    toast("권한 오류", false);
                }
            } catch (Exception e) {
                toast("저장된 파일이 없습니다.", false);
                e.printStackTrace();
            }
        } else {
            requestPerms();
        }
    }*/

    public void saveName(String name) {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput("nickname.txt", MODE_PRIVATE);
            fos.write(name.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            toast(String.valueOf(e), false);
        } finally {
            try {
                if (fos != null) fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String loadName() {
        FileInputStream fis = null;

        try {
            fis = openFileInput("nickname.txt");
            byte[] memoData = new byte[fis.available()];

            while(fis.read(memoData) != -1) {}
            return new String(memoData);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "/*null*/";
    }

    private void toast(String message, boolean longer) {
        int length;
        if (longer) length = Toast.LENGTH_LONG;
        else length = Toast.LENGTH_SHORT;
        Toast.makeText(getApplicationContext(), message, length).show();
    }

    /*private boolean hasPermissions() {
        int res = 0;
        String[] permissions = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

        for (String perms : permissions) {
            res = checkCallingOrSelfPermission(perms);
            if (!(res == PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }
        return true;
    }

    private void requestPerms() {
        String[] permissions = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, 0);
        }
    }*/

    private void checkFull(String type) {
        isFull = true;
        for (int index = 0; index < 8; index++) {
            mReference = mDatabase.getReference(type+"/Member"+(index+1));
            final int position_index = index;
            mReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot messageData : snapshot.getChildren()) {
                        if (messageData.getKey().toString().equals("name") && messageData.getValue().toString().equals("none")) {
                            isFull = false;
                            position = position_index + 1;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
            case R.id.action_btn1:
                for (int index = 0; index < 8; index++) {
                    mReference = mDatabase.getReference("IronHorse/Member"+(index+1));
                    mReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String[] named = new String[4];
                            for (DataSnapshot messageData : snapshot.getChildren()) {
                                switch (messageData.getKey().toString()) {
                                    case "Named1":
                                        named[0] = messageData.getValue().toString();
                                        break;
                                    case "Named2":
                                        named[1] = messageData.getValue().toString();
                                        break;
                                    case "Named3":
                                        named[2] = messageData.getValue().toString();
                                        break;
                                    case "Named4":
                                        named[3] = messageData.getValue().toString();
                                        break;
                                    case "name":
                                        if (edtName.getText().toString().equals(messageData.getValue().toString())) {
                                            for (int i = 0; i < named.length; i++) {
                                                btnNamed[i].setText(named[i]);
                                            }
                                            toast("역할을 불러왔습니다.", false);
                                        }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu, menu);
        return true;
    }
}
