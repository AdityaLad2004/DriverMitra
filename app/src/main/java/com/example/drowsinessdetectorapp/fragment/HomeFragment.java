package com.example.drowsinessdetectorapp.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.drowsinessdetectorapp.R;
import com.example.drowsinessdetectorapp.activity.MainActivity;

public class HomeFragment extends Fragment {
    private Button homeStartButton;
    private Animation button_click;
    private ImageView i2,i3;
    int SELECT_PICTURE=200;
    private ImageButton hamburgerImageButton;
    private DrawerLayout startingDrawerLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        startingDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.startingDrawerLayout);

        button_click = AnimationUtils.loadAnimation(getContext(),R.anim.button_click);
        hamburgerImageButton = (ImageButton) view.findViewById(R.id.hamburgerImageButton);
        i2=(ImageView) view.findViewById(R.id.imageView2);
        i3=(ImageView) view.findViewById(R.id.imageView3);
        hamburgerImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hamburgerImageButton.startAnimation(button_click);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!startingDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                            startingDrawerLayout.openDrawer(GravityCompat.START);
                        } else {
                            startingDrawerLayout.closeDrawer(GravityCompat.START);
                        }
                    }
                },200);
            }
        });

        i2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });

        homeStartButton = (Button) view.findViewById(R.id.homeStartButton);

        homeStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeStartButton.startAnimation(button_click);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getContext(),MainActivity.class);
                        startActivity(intent);

                        /*if (getActivity() != null)
                            getActivity().finish();*/
                    }
                },100);
            }
        });

        return view;
    }

    void imageChooser() {


        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);


        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {


            if (requestCode == SELECT_PICTURE) {

                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {

                    i2.setImageURI(selectedImageUri);
                }
            }
        }
    }



    }







