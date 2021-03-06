package com.liftyourheads.dailyreadings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class fragment_bible extends Fragment {

    private Integer readingNum;

    private ListView bibleListView;
    private TextView bibleTitleTextView;

    public static fragment_bible newInstance(int readingNum) {
        fragment_bible fragment = new fragment_bible();
        Bundle args = new Bundle();
        args.putInt("readingNum", readingNum);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readingNum = getArguments().getInt("readingNum");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Initialise relevant views
        View view = inflater.inflate(R.layout.bible_content, container, false);

        //Initialise elements
        bibleListView = view.findViewById(R.id.bibleListView);
        ScrollView biblePassageScrollView = view.findViewById(R.id.biblePassageScrollView);
        TextView biblePassageTextView = view.findViewById(R.id.biblePassageTextView);
        bibleTitleTextView = getActivity().findViewById(R.id.bibleTitleTextView);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean paragraphs = sharedPreferences.getBoolean("pref_paragraphs", false);

        if (paragraphs) {

            //Settings for paragraph view (hide ListView, show ScrollView)

            biblePassageScrollView.setVisibility(View.VISIBLE);
            biblePassageTextView.setVisibility(View.VISIBLE);
            bibleListView.setVisibility(View.GONE);


            biblePassageTextView.setText(Html.fromHtml(activity_date.readings[readingNum].getVerseParagraphs()));

        } else {

            //Settings for ListView (show ListView, hide ScrollView)

            biblePassageScrollView.setVisibility(View.GONE);
            biblePassageTextView.setVisibility(View.GONE);
            bibleListView.setVisibility(View.VISIBLE);

            setBibleListContent();

        }

        return view;
    }

    public void setBibleListContent() {

        SimpleAdapter bibleSA = new SimpleAdapter(getActivity(), activity_date.readings[readingNum].getReadingVerses(),
                R.layout.verse,
                new String[]{"verseNum", "verseContent"},
                new int[]{R.id.verseNum, R.id.verseContent});

        bibleListView.setAdapter(bibleSA);

        // Setup scroll listener for Bible content

        bibleListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (!activity_date.readings[readingNum].isMultipleBooks()) {

                    int numChapters = activity_date.readings[readingNum].getNumChapters();
                    String readingName = activity_date.readings[readingNum].getBookName(null);
                    Integer[] readingChapters = activity_date.readings[readingNum].getChapters();

                    for (int i = 0; i < numChapters; i++) {


                        if (firstVisibleItem == activity_date.readings[readingNum].getChapterStartCounters(i)) {

                            Log.i("BibleListView Position", "Reached chapter " + readingChapters[(i + 1)] + " at item " + firstVisibleItem);
                            String title = readingName + " " + readingChapters[(i + 1)];
                            bibleTitleTextView.setText(title);

                        } else if (firstVisibleItem == (activity_date.readings[readingNum].getChapterStartCounters(i) - 1)) {

                            Log.i("BibleListView Position", "Reached chapter " + readingChapters[i] + " at item " + firstVisibleItem);
                            String title = readingName + " " + readingChapters[i];
                            bibleTitleTextView.setText(title);

                        }

                    }

                } else {

                    int numChapters = activity_date.readings[readingNum].getNumChapters();

                    for (Integer i = 0; i < numChapters; i++) {



                        if (firstVisibleItem == activity_date.readings[readingNum].getChapterStartCounters(i)) {

                            String readingName = activity_date.readings[readingNum].getBookName(i.toString());
                            Log.i("BibleListView Position", "Reached book " + readingName + " at item " + firstVisibleItem);
                            String title = readingName;
                            bibleTitleTextView.setText(title);

                        } else if (firstVisibleItem == (activity_date.readings[readingNum].getChapterStartCounters(i) - 1)) {

                            String readingName = activity_date.readings[readingNum].getBookName(Integer.toString((i-1)));

                            Log.i("BibleListView Position", "Reached book " + readingName + " at item " + firstVisibleItem);

                            String title = readingName;
                            bibleTitleTextView.setText(title);

                        }

                    }

                }

            }
        });

    }
}

