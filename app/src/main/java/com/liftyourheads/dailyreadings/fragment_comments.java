package com.liftyourheads.dailyreadings;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class fragment_comments extends Fragment {

    private Integer readingNum;
    private SwipeRefreshLayout commentsSwipeRefresh;
    ProgressBar getCommentsProgressBar;

    public static Boolean isRefreshing = false; // Default state for screen refresh
    private static String[] readingChunks; //Separate into 3 readings
    private static String[][] allComments; // Extract individual notes
    int readingComments; // Used for updating comments
    public static Boolean commentsDownloaded = false; // Check if comments are downloaded
    TextView commentsConnectErrorText;

    ListView readingNotesListView;
    private static SimpleAdapter commentsSA;

    private static int[] notesSize;
    private static int maxNotes;
    private static String[][][] commentPost; //Separate note from poster info

    //Main Activity Elements
    TextView firstReadingTextView;
    TextView secondReadingTextView;
    TextView thirdReadingTextView;

    public static Integer curMonth = activity_date.curMonth;
    public static Integer curDay = activity_date.curDay;

    public static fragment_comments newInstance(int number) {
        fragment_comments fragment = new fragment_comments();
        Bundle args = new Bundle();
        args.putInt("readingNum", number);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        readingNum = getArguments().getInt("readingNum", 1);

        //View mainActivityView = getActivity().findViewById(R.id.firstReadingTextView);

        //Get elements from mainActivity

        /*
        firstReadingTextView = getActivity().findViewById(R.id.firstReadingTextView);
        secondReadingTextView = getActivity().findViewById(R.id.secondReadingTextView);
        thirdReadingTextView = getActivity().findViewById(R.id.thirdReadingTextView);
        */


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Initialise relevant views
        View view = inflater.inflate(R.layout.fragment_comments, container, false);

        //Initialise elements in fragment
        readingNotesListView = view.findViewById(R.id.readingNotesListView);

        System.out.println("Processing reading comments: " + readingNum.toString());

        updateNotesListView(readingNum - 1);

        return view;

    }

    public void updateNotesListView(int reading) {

        commentsSA = new SimpleAdapter(getActivity(), activity_date.commentList.get(reading),
                R.layout.note,
                new String[]{"comment", "poster"},
                new int[]{R.id.line_a, R.id.line_b});

        readingNotesListView.setAdapter(commentsSA);

        /*
        // Re-enable clicking now that comments are loaded!!
        firstReadingTextView.setClickable(true);
        secondReadingTextView.setClickable(true);
        thirdReadingTextView.setClickable(true);
        */

        /*if (isRefreshing) {

            commentsSwipeRefresh.setRefreshing(false);
            isRefreshing = false;

        }*/

    }

}
