package com.vip.raid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MemberAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Member> itemList;
    private Activity activity;

    private boolean isManagement = false;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;

    private AlertDialog alertDialog = null;
    private AlertDialog.Builder builder = null;
    private View view;

    public MemberAdapter(Context context, ArrayList<Member> itemList, Activity activity) {
        this.context = context;
        this.itemList = itemList;
        this.activity = activity;
        mDatabase = FirebaseDatabase.getInstance();
    }

    public boolean isManagement() {
        return isManagement;
    }

    public void setManagement(boolean management) {
        isManagement = management;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) convertView = View.inflate(context, R.layout.listitem, null);

        TextView txtName = convertView.findViewById(R.id.txtName);
        ImageView imgGrade = convertView.findViewById(R.id.imgGrade);
        TextView txtDate = convertView.findViewById(R.id.txtDate);
        TextView txtStartDate = convertView.findViewById(R.id.txtStartDate);
        TextView txtLength = convertView.findViewById(R.id.txtLength);
        LinearLayout layoutUnconnect = convertView.findViewById(R.id.layoutUnconnect);
        LinearLayout layoutManagement = convertView.findViewById(R.id.layoutManagement);
        Button btnEdit = convertView.findViewById(R.id.btnEdit);
        Button btnDelete = convertView.findViewById(R.id.btnDelete);

        if (isManagement) layoutManagement.setVisibility(View.VISIBLE);
        else layoutManagement.setVisibility(View.GONE);

        txtName.setText(itemList.get(position).getName());
        txtDate.setText(itemList.get(position).getDate());

        switch (itemList.get(position).getGrade()) {
            case "수습 요원":
                imgGrade.setImageResource(R.drawable.difficulty1);
                break;
            case "요원":
                imgGrade.setImageResource(R.drawable.difficulty2);
                break;
            case "부관":
                imgGrade.setImageResource(R.drawable.difficulty3);
                break;
            case "지휘관":
                imgGrade.setImageResource(R.drawable.difficulty4);
                break;
        }

        if (itemList.get(position).isUnconnect()) {
            layoutUnconnect.setVisibility(View.VISIBLE);
            txtStartDate.setText(itemList.get(position).getStartdate());
            txtLength.setText(itemList.get(position).getDate_length()+"일");
        } else layoutUnconnect.setVisibility(View.GONE);

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MemberAddActivity.class);
                intent.putExtra("isEdit", true);
                intent.putExtra("Name", itemList.get(position).getName());
                context.startActivity(intent);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View view = activity.getLayoutInflater().inflate(R.layout.deletelayout, null);

                final TextView txtContent = view.findViewById(R.id.txtContent);
                final Button btnDelete = view.findViewById(R.id.btnDelete);
                final Button btnCancel = view.findViewById(R.id.btnCancel);

                txtContent.setText(itemList.get(position).getName()+"님의 정보를 삭제하시겠습니까?");

                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mReference = mDatabase.getReference("Users");
                        mReference.child(itemList.get(position).getName()).removeValue();
                        Toast.makeText(context, itemList.get(position).getName()+"님의 정보가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        itemList.remove(position);
                        notifyDataSetChanged();
                        alertDialog.dismiss();
                    }
                });

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                builder = new AlertDialog.Builder(activity);
                builder.setView(view);

                alertDialog =builder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertDialog.show();
            }
        });

        return convertView;
    }
}
