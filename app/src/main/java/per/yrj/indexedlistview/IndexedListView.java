package per.yrj.indexedlistview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;


import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by YiRenjie on 2016/5/17.
 */
public class IndexedListView extends FrameLayout {
    private char[] mNameIndex;
    private HanyuPinyinOutputFormat mFormat;
    private ListView mListView;
    private IndexView mIndexView;

    public IndexedListView(Context context) {
        this(context, null);
    }

    public IndexedListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mFormat = new HanyuPinyinOutputFormat();
        mFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);

        mListView = new ListView(getContext());
        mIndexView = new IndexView(getContext());

        addView(mListView);
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                LayoutParams params = new LayoutParams(getWidth()/12, LayoutParams.MATCH_PARENT, Gravity.RIGHT);
                params.rightMargin = 5;
                mIndexView.setLayoutParams(params);
                IndexedListView.this.addView(mIndexView);
            }
        });
        mIndexView.setOnIndexChangedListener(new IndexView.OnIndexChangedListener() {
            @Override
            public void onIndexChanged(char index) {
                for (int i = 0; i < mNameIndex.length; i++) {
                    if(index == mNameIndex[i]){
                        mListView.setSelection(i);
                        return;
                    }
                }
            }
        });
    }

    public void setData(String[] names){
        sortNames(names);

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < names.length; i++){
            char firstName = names[i].toCharArray()[0];
            char c = 0;
            try {
                c = PinyinHelper.toHanyuPinyinStringArray(firstName, mFormat)[0].charAt(0);
            } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                badHanyuPinyinOutputFormatCombination.printStackTrace();
            }
            sb.append(c);
        }
            mNameIndex = sb.toString().toCharArray();

        mListView.setAdapter(new MyAdapter(names));
        invalidate();
    }

    private void sortNames(String[] names) {
        Arrays.sort(names, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                char firstName1 = lhs.charAt(0);
                char firstName2 = rhs.charAt(0);

                char firstLetter1 = 0;
                char firstLetter2 = 0;
                try {
                    firstLetter1 = PinyinHelper.toHanyuPinyinStringArray(firstName1, mFormat)[0].charAt(0);
                    firstLetter2 = PinyinHelper.toHanyuPinyinStringArray(firstName2, mFormat)[0].charAt(0);
                } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                    badHanyuPinyinOutputFormatCombination.printStackTrace();
                }

                return firstLetter1 - firstLetter2;
            }
        });
    }

    class MyAdapter extends BaseAdapter {
        private String[] mNames;

        public MyAdapter(String[] names){
            this.mNames = names;
        }

        @Override
        public int getCount() {
            return mNames.length;
        }

        @Override
        public Object getItem(int position) {
            return mNames[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                convertView = View.inflate(getContext(), R.layout.list_item, null);
                holder = new ViewHolder();
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tvIndex = (TextView) convertView.findViewById(R.id.tv_index);
            holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);

            holder.tvIndex.setText(mNameIndex[position]+"");
            if(position > 0){
                holder.tvIndex.setVisibility(mNameIndex[position]==mNameIndex[position-1]?GONE:VISIBLE);
            }
            holder.tvName.setText(mNames[position]);
            return convertView;
        }

        class ViewHolder{
            TextView tvIndex;
            TextView tvName;
        }
    }

    static class IndexView extends View {
        public final char[] LETTERS = {'A','B','C','D','E','F','G',
                'H','I','J','K','L','M','N','O','P','Q','R','S','T',
                'U','V','W','X','Y','Z'};
        private OnIndexChangedListener mListener;
        private float cellHeight;
        private float cellWidth;

        public IndexView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            cellHeight = (canvas.getHeight()-10)/LETTERS.length;
            cellWidth = canvas.getWidth();

            //draw background of index letters
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setAlpha(112);
            float l = getWidth() - cellWidth;
            float t = getTop();
            float r = l + cellWidth;
            float b = getBottom();
            float round = cellWidth/2;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                canvas.drawRoundRect(l,t,r,b,round, round,paint);
            }else {
                canvas.drawRect(l, t, r, b, paint);
            }

            //draw index letters
            float x = canvas.getWidth() - cellWidth/2;
            float y = cellHeight/2 + 10;

            paint.reset();
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(cellHeight*0.8f);

            for (int i = 0; i < LETTERS.length; i++) {
                paint.setColor(LETTERS[i] == lastIndex? Color.RED: Color.WHITE);
                canvas.drawText(LETTERS, i, 1, x, y+i*cellHeight, paint);
            }
        }
        private char lastIndex;
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            char curIndex;
            switch (event.getAction()){
                case MotionEvent.ACTION_MOVE:
                    curIndex = getIndex(event.getY());
                    if(curIndex != lastIndex){
                        if(mListener != null) {
                            mListener.onIndexChanged(curIndex);
                        }
                        lastIndex = curIndex;
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    lastIndex = 0;
                    break;
            }
            return true;
        }

        private char getIndex(float y) {
            int index = (int) ((y - 10)/cellHeight);
            return LETTERS[index];
        }

        interface OnIndexChangedListener{
            void onIndexChanged(char index);
        }

        void setOnIndexChangedListener(OnIndexChangedListener listener){
            this.mListener = listener;
        }

    }

}
