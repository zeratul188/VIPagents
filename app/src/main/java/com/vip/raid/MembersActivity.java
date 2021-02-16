package com.vip.raid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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

import java.util.ArrayList;

public class MembersActivity extends AppCompatActivity {

    private AlertDialog alertDialog = null;
    private AlertDialog.Builder builder = null;
    private View view;

    private ListView listView;
    private TextView txtManagement;

    private boolean isManagment = false;
    private MemberAdapter memberAdapter;
    private ArrayList<Member> members;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;

    private Context context = this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memberslayout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("VIP 클랜원 목록");

        listView = findViewById(R.id.listView);
        txtManagement = findViewById(R.id.txtManagement);

        members = new ArrayList<Member>();

        mDatabase = FirebaseDatabase.getInstance();
        /*mReference = mDatabase.getReference("Users");
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getChildrenCount() == 0) {

                }
                for (DataSnapshot messageData : snapshot.getChildren()) {
                    String name = messageData.child("name").getValue().toString();
                    String grade = messageData.child("grade").getValue().toString();
                    String date = messageData.child("date").getValue().toString();
                    String startdate = messageData.child("startdate").getValue().toString();
                    int date_length = Integer.parseInt(messageData.child("date_length").getValue().toString());
                    boolean isUnconnect = Boolean.parseBoolean(messageData.child("unconnect").getValue().toString());
                    Member member = new Member(name, grade, date, startdate, date_length, isUnconnect);
                    members.add(member);
                }

                memberAdapter = new MemberAdapter(context, members);
                if (members.size() != 0) listView.setAdapter(memberAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        members.clear();
        mReference = mDatabase.getReference("Users");
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot messageData : snapshot.getChildren()) {
                    String name = messageData.child("name").getValue().toString();
                    String grade = messageData.child("grade").getValue().toString();
                    String date = messageData.child("date").getValue().toString();
                    String startdate = messageData.child("startdate").getValue().toString();
                    int date_length = Integer.parseInt(messageData.child("date_length").getValue().toString());
                    boolean isUnconnect = Boolean.parseBoolean(messageData.child("unconnect").getValue().toString());
                    Member member = new Member(name, grade, date, startdate, date_length, isUnconnect);
                    members.add(member);
                }

                memberAdapter = new MemberAdapter(context, members, MembersActivity.this);
                memberAdapter.setManagement(isManagment);
                if (members.size() != 0) listView.setAdapter(memberAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
            case R.id.action_btn1:
                view = getLayoutInflater().inflate(R.layout.managementlayout, null);

                final EditText edtPassword = view.findViewById(R.id.edtPassword);
                final TextView txtInfo = view.findViewById(R.id.txtInfo);
                final Button btnGet = view.findViewById(R.id.btnGet);
                final Button btnCancel = view.findViewById(R.id.btnCancel);

                if (isManagment) {
                    edtPassword.setVisibility(View.GONE);
                    txtInfo.setText("권한을 제거하시겠습니까?");
                    txtInfo.setTextColor(Color.parseColor("#f0f0f0"));
                    btnGet.setText("권한 제거");
                }

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                btnGet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isManagment) {
                            isManagment = false;
                            memberAdapter.setManagement(false);
                            memberAdapter.notifyDataSetChanged();
                            txtManagement.setVisibility(View.GONE);
                            alertDialog.dismiss();
                            return;
                        }
                        if (edtPassword.getText().toString().equals("5878")) {
                            isManagment = true;
                            memberAdapter.setManagement(true);
                            memberAdapter.notifyDataSetChanged();
                            txtManagement.setVisibility(View.VISIBLE);
                        } else Toast.makeText(getApplicationContext(), "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    }
                });

                builder = new AlertDialog.Builder(this);
                builder.setView(view);

                alertDialog =builder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();

                return true;
            case R.id.action_btn2:
                if (!isManagment) {
                    Toast.makeText(getApplicationContext(), "추가할 권한이 없습니다.", Toast.LENGTH_SHORT).show();
                    return true;
                }
                Intent intent = new Intent(getApplicationContext(), MemberAddActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.members_menu, menu);
        return true;
    }
}
