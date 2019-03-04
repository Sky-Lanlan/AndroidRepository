package com.example.lan.mymusicapp.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.lan.mymusicapp.R;
import com.example.lan.mymusicapp.adapter.ListAdapter;
import com.example.lan.mymusicapp.musicItem.MediaUtil;
import com.example.lan.mymusicapp.musicItem.Mp3Info;
import com.example.lan.mymusicapp.musicItem.MusicItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.lan.mymusicapp.playMode.Play;
import com.master.permissionhelper.PermissionHelper;

public class Fragment1 extends Fragment {
    ListAdapter adapter;
    String lastInfo = null;
    private String TAG = "MusicActivity";
    private List<MusicItem> musicItems = new ArrayList<>();
    List<Mp3Info> mp3Infos = new ArrayList<>();
    List<HashMap<String, String>> list = new ArrayList<>();
    private PermissionHelper permissionHelper;
    private Handler handler = null;
    private ProgressBar progressBar1;
    private TextView current_time;
    private TextView end_time;
    private View view = null;



    @Override
    public void onStart() {
        super.onStart();
        Play.reloadMusic(getActivity(), handler, (TextView) view.findViewById(R.id.pMusicName));
        Log.d("Fragment", "step into onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
//        adapter.getMyMusicThread().setSuspend(false);




        Log.d("Fragment", "step into onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("Fragment", "step into onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("Fragment", "step into onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();



        Log.d("Fragment", "step into onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("Fragment", "step into onDetach");
    }




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        view = inflater.inflate(R.layout.fragment1, container, false);


        // 关于权限的代码
        permissionHelper = new PermissionHelper(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        permissionHelper.request(new PermissionHelper.PermissionCallback() {
            @SuppressLint("HandlerLeak")
            @Override
            public void onPermissionGranted() {

                mp3Infos = MediaUtil.getMp3Infos(getActivity());
                list = MediaUtil.getMusicMaps(mp3Infos);
                initMusicItem(list);
                RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

                setProgress();
                adapter = new ListAdapter(musicItems,
                        (TextView) view.findViewById(R.id.pMusicName), handler, getActivity(),
                        (ImageView) view.findViewById(R.id.album), (ImageView)view.findViewById(R.id.control));
                recyclerView.setAdapter(adapter);


                adapter.addFooterView(LayoutInflater.from(getActivity()).inflate(R.layout.item_footer_layout, null));
                adapter.addHeaderView(LayoutInflater.from(getActivity()).inflate(R.layout.item_header_layout, null));
                recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
                recyclerView.setLayoutManager(layoutManager);
                Log.d(TAG, "onPermissionGranted() called");
            }

            @Override
            public void onIndividualPermissionGranted(String[] grantedPermission) {
                Log.d(TAG, "onIndividualPermissionGranted() called with: grantedPermission = [" + TextUtils.join(",", grantedPermission) + "]");
            }

            @Override
            public void onPermissionDenied() {
                Log.d(TAG, "onPermissionDenied() called");
            }

            @Override
            public void onPermissionDeniedBySystem() {
                Log.d(TAG, "onPermissionDeniedBySystem() called");
            }
        });
// 权限代码结束

        return view;
    }

    // 权限代码
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionHelper != null) {
            permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }


    private void initMusicItem(List<HashMap<String, String>> list) {

        for (int i = 0; i < list.size(); i++) {
            String title = list.get(i).get("title");
            String artist = list.get(i).get("Artist");
            String album = list.get(i).get("album");
            String displayName = list.get(i).get("displayName");
            long albumId = Long.parseLong(list.get(i).get("albumId"));
            String duration = list.get(i).get("duration");
            long size = Long.parseLong(list.get(i).get("size"));
            String url = list.get(i).get("url");
            MusicItem item = new MusicItem(i + 1, title, album, albumId, displayName, artist, duration, size, url);
            musicItems.add(item);
        }


    }

    @SuppressLint("HandlerLeak")
    public void setProgress() {
        handler = new Handler() {
            public void handleMessage(Message meg) {
                progressBar1 = view.findViewById(R.id.progressBar1);
                current_time = view.findViewById(R.id.current_time);
                end_time = view.findViewById(R.id.end_time);
                String[] receive = ((String) meg.obj).split(":");

                int max = Integer.parseInt(receive[1]);
                int cur = Integer.parseInt(receive[0]);
                if (cur <= 1) {
                    progressBar1.setMax(max / 1000);
                    end_time.setText(MediaUtil.formaTime(max));
                }
                current_time.setText(MediaUtil.formaTime((cur+1) * 1000));
                progressBar1.setProgress(cur);


            }

        };
    }




}