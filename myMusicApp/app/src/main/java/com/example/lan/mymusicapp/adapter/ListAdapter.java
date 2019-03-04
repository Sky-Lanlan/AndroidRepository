package com.example.lan.mymusicapp.adapter;


import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.example.lan.mymusicapp.musicItem.MusicItem;
import com.example.lan.mymusicapp.R;
import com.example.lan.mymusicapp.myMusicThread.MyMusicThread;
import com.example.lan.mymusicapp.playMode.Play;


import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.example.lan.mymusicapp.R.drawable.album;


public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private List<MusicItem> mMusicItemList;
//    private MediaPlayer mediap;
    private String lastUrl;
    private TextView pMusicName;
    private MyMusicThread myMusicThread;
    private Handler handler;
    private RecyclerView mRecyclerView;
    private ImageView album;
    private ImageView control;
    private Boolean isPlay = false;


    private View VIEW_FOOTER;
    private View VIEW_HEADER;
    private Context mContext;

    private int TYPE_NORMAL = 1000;
    private int TYPE_HEADER = 1001;
    private int TYPE_FOOTER = 1002;


    static class ViewHolder extends RecyclerView.ViewHolder {

        View musicView;
        TextView index;
        TextView musicName;
        TextView singer;
        TextView total_time;


        ViewHolder(View view) {
            super(view);

            musicView = view;
            index = view.findViewById(R.id.index);
            musicName = view.findViewById(R.id.music_name);
            singer = view.findViewById(R.id.singer);
            total_time = view.findViewById(R.id.total_time);


        }

    }

    public ListAdapter(List<MusicItem> musicItemsList, TextView textView, Handler handler,
                       Context context, ImageView imageView, ImageView control) {
        mMusicItemList = musicItemsList;
        pMusicName = textView;
        this.handler = handler;
        mContext = context;
        album = imageView;
        this.control = control;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, int viewType) {


        if (viewType == TYPE_FOOTER) {
            return new ViewHolder(VIEW_FOOTER);
        } else if (viewType == TYPE_HEADER) {
            return new ViewHolder(VIEW_HEADER);
        } else {
            final View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.music_item, viewGroup, false);
            final ViewHolder holder = new ViewHolder(view);


            PlayListener playListener = new PlayListener(holder);
            holder.musicView.setOnClickListener(playListener);
            control.setOnClickListener(playListener);



            return holder;
        }
    }


    private class PlayListener implements View.OnClickListener{
        private ViewHolder holder;
        PlayListener(ViewHolder holder) {
            this.holder = holder;
        }

        @Override
        public void onClick(View v) {
            if (v.getId() != R.id.control) {
                int position = holder.getAdapterPosition();
                MusicItem musicItem = mMusicItemList.get(position - 1);
                String nextPlayMusic = musicItem.getTitle();
                pMusicName.setText(nextPlayMusic);
                String url = musicItem.getUrl();
                isPlay = Play.setPlayMode(mMusicItemList, position - 1, url, lastUrl,
                        handler, false, control);
                lastUrl = url;
            }else {
                if (Play.url2Save == null) return;
                isPlay = !isPlay;
                if (isPlay){
                    control.setImageResource(R.drawable.ic_play_btn_pause);
                    Play.toPlay();
                }else {
                    control.setImageResource(R.drawable.ic_play_btn_play);
                    Play.tPause();
                }

            }

        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (!isHeaderView(position) && !isFooterView(position)) {
            if (haveHeaderView()) position--;

            MusicItem musicItem = mMusicItemList.get(position);
            holder.index.setText(musicItem.getId() + "");
            holder.musicName.setText(musicItem.getTitle());
            holder.total_time.setText(musicItem.getDuration());
            //设置加粗字体
            TextPaint paint = holder.musicName.getPaint();
            paint.setFakeBoldText(true);
            holder.singer.setText(musicItem.getArtist());
        }
    }

    @Override
    public int getItemCount() {
        int count = (mMusicItemList == null ? 0:mMusicItemList.size());
        if (VIEW_FOOTER != null){
            count++;
        }
        if (VIEW_HEADER != null){
            count++;
        }
        return count;
    }

    private boolean haveHeaderView() {
        return VIEW_HEADER != null;
    }

    public boolean haveFooterView() {
        return VIEW_FOOTER != null;
    }

    private boolean isHeaderView(int position) {
        return haveHeaderView() && position == 0;
    }

    private boolean isFooterView(int position) {
        return haveFooterView() && position == getItemCount() - 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderView(position)){
            return TYPE_HEADER;
        }else if (isFooterView(position)){
            return TYPE_FOOTER;
        }else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        try {
            if (mRecyclerView == null && mRecyclerView != recyclerView){
                mRecyclerView = recyclerView;
            }
            ifGridLayoutManager();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void ifGridLayoutManager() {
        if (mRecyclerView == null) return;
        final RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager.SpanSizeLookup originalSpanSizeLookup =
                    ((GridLayoutManager) layoutManager).getSpanSizeLookup();
            ((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return (isHeaderView(position) || isFooterView(position)) ?
                            ((GridLayoutManager) layoutManager).getSpanCount() :
                            1;
                }
            });
        }

    }

    private View getLayout(int layoutId) {
        return LayoutInflater.from(mContext).inflate(layoutId, null);
    }

    public void addHeaderView(View headerView) {
        Log.d("ListAdapter", "addHeaderView called");

        EditText editText = headerView.findViewById(R.id.edit_search);
        editText.requestFocus();
        editText.requestFocusFromTouch();
        if (haveHeaderView()) {
            throw new IllegalStateException("hearview has already exists!");
        } else {
            //避免出现宽度自适应
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            headerView.setLayoutParams(params);
            VIEW_HEADER = headerView;
            ifGridLayoutManager();
            notifyItemInserted(0);
        }

    }

    public void addFooterView(View footerView) {
        if (haveFooterView()) {
            throw new IllegalStateException("footerView has already exists!");
        } else {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            footerView.setLayoutParams(params);
            VIEW_FOOTER = footerView;
            ifGridLayoutManager();
            notifyItemInserted(getItemCount() - 1);
        }
    }



    public static Bitmap getArtwork(Context context, long song_id, long album_id,
                                    boolean allowdefault) {
        if (album_id < 0) {
            // This is something that is not in the database, so get the album art directly
            // from the file.
            if (song_id >= 0) {
                Bitmap bm = getArtworkFromFile(context, song_id, -1);
                if (bm != null) {
                    return bm;
                }
            }
            if (allowdefault) {
                return getDefaultArtwork(context);
            }
            return null;
        }
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        if (uri != null) {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in, null, sBitmapOptions);
            } catch (FileNotFoundException ex) {
                // The album art thumbnail does not actually exist. Maybe the user deleted it, or
                // maybe it never existed to begin with.
                Bitmap bm = getArtworkFromFile(context, song_id, album_id);
                if (bm != null) {
                    if (bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false);
                        if (bm == null && allowdefault) {
                            return getDefaultArtwork(context);
                        }
                    }
                } else if (allowdefault) {
                    bm = getDefaultArtwork(context);
                }
                return bm;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return null;
    }

    private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
        Bitmap bm = null;
        byte [] art = null;
        String path = null;
        if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        try {
            if (albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            } else {
                Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            }
        } catch (FileNotFoundException ex) {

        }
        if (bm != null) {
            mCachedBit = bm;
        }
        return bm;
    }

    @SuppressLint("ResourceType")
    private static Bitmap getDefaultArtwork(Context context) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeStream(
                context.getResources().openRawResource(R.drawable.album), null, opts);
    }
    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    private static Bitmap mCachedBit = null;

}
