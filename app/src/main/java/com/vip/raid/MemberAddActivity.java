package com.vip.raid;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MemberAddActivity extends AppCompatActivity {

    private EditText edtName, edtLength;
    private RadioGroup rgGrade;
    private RadioButton[] rdoGrade = new RadioButton[4];
    private TextView txtDate, txtStartDate;
    private Button btnDate, btnStartDate, btnAdd, btnNow, btnDouble;
    private CheckBox chkUnconnect;
    private LinearLayout layoutUnconnect;

    private AlertDialog alertDialog = null;
    private AlertDialog.Builder builder = null;
    private View view;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;

    private int count = 0;
    private boolean isDouble = false, isEdit = false;
    private String name = "null";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memberaddlayout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("클랜원 추가");

        edtName = findViewById(R.id.edtName);
        edtLength = findViewById(R.id.edtLength);
        rgGrade = findViewById(R.id.rgGrade);
        txtDate = findViewById(R.id.txtDate);
        txtStartDate = findViewById(R.id.txtStartDate);
        btnDate = findViewById(R.id.btnDate);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnAdd = findViewById(R.id.btnAdd);
        chkUnconnect = findViewById(R.id.chkUnconnect);
        btnNow = findViewById(R.id.btnNow);
        layoutUnconnect = findViewById(R.id.layoutUnconnect);
        btnDouble = findViewById(R.id.btnDouble);

        for (int i = 0; i < rdoGrade.length; i++) {
            int resources = getResources().getIdentifier("rdoGrade"+(i+1), "id", getPackageName());
            rdoGrade[i] = findViewById(resources);
        }

        Intent intent = getIntent();
        isEdit = intent.getBooleanExtra("isEdit", false);
        name = intent.getStringExtra("Name");

        mDatabase = FirebaseDatabase.getInstance();

        if (isEdit) {
            edtName.setText(name);
            edtName.setEnabled(false);
            btnDouble.setEnabled(false);
            btnDouble.setTextColor(Color.parseColor("#FF4444"));
            isDouble = true;
            setTitle(name+"님의 정보");
            btnAdd.setText("사용자 수정");

            mReference = mDatabase.getReference("Users");
            mReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot messageData : snapshot.getChildren()) {
                        if (name.equals(messageData.child("name").getValue())) {
                            if ((boolean)messageData.child("unconnect").getValue()) {
                                chkUnconnect.setChecked(true);
                                layoutUnconnect.setVisibility(View.VISIBLE);
                            } else {
                                chkUnconnect.setChecked(false);
                                layoutUnconnect.setVisibility(View.GONE);
                            }
                            switch (messageData.child("grade").getValue().toString()) {
                                case "수습 요원":
                                    rdoGrade[0].setChecked(true);
                                    break;
                                case "요원":
                                    rdoGrade[1].setChecked(true);
                                    break;
                                case "부관":
                                    rdoGrade[2].setChecked(true);
                                    break;
                                case "지휘관":
                                    rdoGrade[3].setChecked(true);
                                    break;
                            }
                            txtDate.setText(messageData.child("date").getValue().toString());
                            txtStartDate.setText(messageData.child("startdate").getValue().toString());
                            edtLength.setText(messageData.child("date_length").getValue().toString());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        mReference = mDatabase.getReference("Users");
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                count = (int) snapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(calendar.YEAR);
        int month = calendar.get(calendar.MONTH)+1;
        int day = calendar.get(calendar.DATE);

        txtDate.setText(year+"년 "+month+"월 "+day+"일");
        txtStartDate.setText(year+"년 "+month+"월 "+day+"일");

        btnDouble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtName.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "아이디를 입력하십시오.", Toast.LENGTH_SHORT).show();
                    return;
                }
                mReference = mDatabase.getReference("Users");
                mReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isDouble = true;
                        if (snapshot.getChildrenCount() == 0) {
                            Toast.makeText(getApplicationContext(), "중복된 아이디가 없습니다.", Toast.LENGTH_SHORT).show();
                            edtName.setEnabled(false);
                            return;
                        }
                        for (DataSnapshot messageData : snapshot.getChildren()) {
                            String name = messageData.child("name").getValue().toString();
                            String grade = messageData.child("grade").getValue().toString();
                            String date = messageData.child("date").getValue().toString();
                            String startdate = messageData.child("startdate").getValue().toString();
                            int date_length = Integer.parseInt(messageData.child("date_length").getValue().toString());
                            boolean isUnconnect = Boolean.parseBoolean(messageData.child("unconnect").getValue().toString());
                            Member member = new Member(name, grade, date, startdate, date_length, isUnconnect);
                            if (edtName.getText().toString().equals(member.getName())) {
                                isDouble = false;
                                Toast.makeText(getApplicationContext(), "중복된 아이디가 존재합니다.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        Toast.makeText(getApplicationContext(), "중복된 아이디가 없습니다.", Toast.LENGTH_SHORT).show();
                        edtName.setEnabled(false);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        btnNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nowDate();
                Toast.makeText(getApplicationContext(), "오늘 날짜를 선택하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view = getLayoutInflater().inflate(R.layout.datelayout, null);

                final DatePicker datePicker = view.findViewById(R.id.datePicker);
                final Button btnChoice = view.findViewById(R.id.btnChoice);
                final Button btnCancel = view.findViewById(R.id.btnCancel);

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                btnChoice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int year = datePicker.getYear();
                        int month = datePicker.getMonth()+1;
                        int day = datePicker.getDayOfMonth();
                        txtDate.setText(year+"년 "+month+"월 "+day+"일");
                        Toast.makeText(getApplicationContext(), year+"년 "+month+"월 "+day+"일로 날짜를 선택하였습니다.", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    }
                });

                builder = new AlertDialog.Builder(MemberAddActivity.this);
                builder.setView(view);

                alertDialog =builder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();
            }
        });

        chkUnconnect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) layoutUnconnect.setVisibility(View.VISIBLE);
                else layoutUnconnect.setVisibility(View.GONE);
            }
        });

        btnStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view = getLayoutInflater().inflate(R.layout.datelayout, null);

                final DatePicker datePicker = view.findViewById(R.id.datePicker);
                final Button btnChoice = view.findViewById(R.id.btnChoice);

                btnChoice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int year = datePicker.getYear();
                        int month = datePicker.getMonth()+1;
                        int day = datePicker.getDayOfMonth();
                        txtStartDate.setText(year+"년 "+month+"월 "+day+"일");
                        Toast.makeText(getApplicationContext(), year+"년 "+month+"월 "+day+"일로 날짜를 선택하였습니다.", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    }
                });

                builder = new AlertDialog.Builder(MemberAddActivity.this);
                builder.setView(view);

                alertDialog =builder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtName.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "아이디를 입력하십시오.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isDouble) {
                    Toast.makeText(getApplicationContext(), "중복 체크를 해주시기 바랍니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String name = edtName.getText().toString();
                String grade = "수습 요원";
                switch (rgGrade.getCheckedRadioButtonId()) {
                    case R.id.rdoGrade1:
                        grade = "수습 요원";
                        break;
                    case R.id.rdoGrade2:
                        grade = "요원";
                        break;
                    case R.id.rdoGrade3:
                        grade = "부관";
                        break;
                    case R.id.rdoGrade4:
                        grade = "지휘관";
                        break;
                }
                String date = txtDate.getText().toString();
                String startdate = txtStartDate.getText().toString();
                int date_length = 0;
                if (!edtLength.getText().toString().equals("")) date_length = Integer.parseInt(edtLength.getText().toString());
                boolean isUnconnect;
                if (chkUnconnect.isChecked()) isUnconnect = true;
                else isUnconnect = false;

                mReference = mDatabase.getReference("Users");
                if (isEdit) {
                    Map<String, Object> taskMap = new HashMap<String, Object>();
                    taskMap.put("date", date);
                    taskMap.put("date_length", date_length);
                    taskMap.put("grade", grade);
                    taskMap.put("name", name);
                    taskMap.put("startdate", startdate);
                    taskMap.put("unconnect", isUnconnect);
                    mReference.child(name).updateChildren(taskMap);
                } else {
                    Member member = new Member(name, grade, date, startdate, date_length, isUnconnect);
                    mReference.child(name).setValue(member);
                }

                finish();
            }
        });

    }

    private void nowDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(calendar.YEAR);
        int month = calendar.get(calendar.MONTH)+1;
        int day = calendar.get(calendar.DATE);

        txtDate.setText(year+"년 "+month+"월 "+day+"일");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
