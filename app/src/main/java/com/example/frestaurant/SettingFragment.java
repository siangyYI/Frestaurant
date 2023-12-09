package com.example.frestaurant;

import android.os.Bundle;
import com.example.frestaurant.EditProfileFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class SettingFragment extends Fragment {
    private FirebaseFirestore db;
    private DocumentReference docRef;
    private Button editButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = currentUser.getEmail();


        docRef = db.collection("User").document(userEmail); // 替换为实际的集合和文档名称
        TextView nameText=rootView.findViewById(R.id.titleName);
        TextView emailText=rootView.findViewById(R.id.profileEmail);
        TextView genderText=rootView.findViewById(R.id.profileUserGender);
        TextView addressText=rootView.findViewById(R.id.profileAddress);
        TextView jobText=rootView.findViewById(R.id.profileJob);
        editButton=rootView.findViewById(R.id.editButton);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // 获取数据
                    String name = documentSnapshot.getString("name");
                    String address = documentSnapshot.getString("address");// 替换为实际的字段名称
                    String email = documentSnapshot.getString("UserEmail");
                    String gender = documentSnapshot.getString("UserGender");
                    String job = documentSnapshot.getString("UserJob");
                    // 处理数据
                    // 在这里，您可以使用获取到的数据执行您的逻辑
                    nameText.setText(name);
                    addressText.setText(address);
                    emailText.setText(email);
                    genderText.setText(gender);
                    jobText.setText(job);
                }
            }
        });
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 打开编辑资料的 Fragment 或 Activity
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                EditProfileFragment editProfileFragment = new EditProfileFragment();

                // 添加一些平滑的动画
                fragmentTransaction.setCustomAnimations(
                        android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out);

                // 替换为新的 Fragment
                fragmentTransaction.replace(R.id.frame_layout, editProfileFragment);
                fragmentTransaction.addToBackStack(null); // 添加到回退栈以支持后退键操作
                fragmentTransaction.commit();
            }
        });
        return rootView;
    }

}