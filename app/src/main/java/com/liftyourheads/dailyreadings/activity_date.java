package com.liftyourheads.dailyreadings;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;


public class activity_date extends AppCompatActivity {

    public static Context CONTEXT;

    Calendar cal;
    public static Integer curMonth;
    public static Integer curDay;
    public static Integer selectedMonth;
    public static Integer selectedDay;

    BottomNavigationView navigation;

    TextView dateTextView;
    TextView firstReadingTextView;
    TextView secondReadingTextView;
    TextView thirdReadingTextView;
    ProgressBar getCommentsProgressBar;

    FrameLayout dateFrameLayout;
    ListView datesListView;
    ListView readingNotesListView;
    ConstraintLayout menuBackgroundConstraint;
    SwipeRefreshLayout commentsSwipeRefresh;
    LinearLayout readingsLayout;

    FragmentManager fragmentManager;

    ArrayList<String> nearbyReadingDates = new ArrayList<>();
    Boolean listViewActive = false;

    public static final String[] MONTHS = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    SimpleDateFormat monthStringTemplate = new SimpleDateFormat("MMMM", Locale.getDefault());

    Boolean isRefreshing = false; // Default state for screen refresh
    String[] readingChunks; //Separate into 3 readings
    String[][] allComments; // Extract individual notes
    int readingComments; // Used for updating comments
    Boolean commentsDownloaded = false;

    int[] notesSize;
    int maxNotes;
    String[][][] commentPost; //Separate note from poster info
    static final String[] BIBLE = {"Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy", "Joshua", "Judges", "Ruth", "1 Samuel", "2 Samuel", "1 Kings", "2 Kings", "1 Chronicles", "2 Chronicles", "Ezra", "Nehemiah", "Esther", "Job", "Psalms", "Proverbs", "Ecclesiastes", "Song of Solomon", "Isaiah", "Jeremiah", "Lamentations", "Ezekiel", "Daniel", "Hosea", "Joel", "Amos", "Obadiah", "Jonah", "Micah", "Nahum", "Habakkuk", "Zephaniah", "Haggai", "Zechariah", "Malachi", "Matthew", "Mark", "Luke", "John", "Acts", "Romans", "1 Corinthians", "2 Corinthians", "Galatians", "Ephesians", "Philippians", "Colossians", "1 Thessalonians", "2 Thessalonians", "1 Timothy", "2 Timothy", "Titus", "Philemon", "Hebrews", "James", "1 Peter", "2 Peter", "1 John", "2 John", "3 John", "Jude", "Revelation"};
    static final String[] BIBLE_PLACES = {"Gen", "Ex", "Lev", "Num", "Deut", "Josh", "Judg", "Ruth", "1 Sam", "2 Sam", "1 Kgs", "2 Kgs", "1 Chr", "2 Chr", "Ezra", "Neh", "Est", "Job", "Ps", "Pro", "Eccl", "Sng", "Isa", "Jer", "Lam", "Ezek", "Dan", "Hos", "Joel", "Amos", "Obad", "Jonah", "Mic", "Nahum", "Hab", "Zeph", "Hag", "Zech", "Mal", "Matt", "Mark", "Luke", "John", "Acts", "Rom", "1 Cor", "2 Cor", "Gal", "Eph", "Phil", "Col", "1 Thes", "2 Thes", "1 Tim", "2 Tim", "Titus", "Phm", "Heb", "James", "1 Pet", "2 Pet", "1 Jn", "2 Jn", "3 Jn", "Jude", "Rev"};
    static final String[] TRANSLATIONS = {"KJV", "ESV", "NET"};

    public static Reading[] readings;
    public static Boolean paragraphs;
    String curTranslation;
    public static Boolean forceRestart = false; //If theme has changed, forces an activity restart
    public static Boolean forceUIOrientationCheck = false;

    TextView commentsConnectErrorText;

    public static ArrayList<ArrayList<HashMap<String, String>>> commentList;
    ViewPager commentsViewPager;
    FragmentPagerAdapter adapterViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        setContentView(R.layout.activity_main);

        CONTEXT = this;

        if (savedInstanceState != null) Log.i("Saved Instance State","Restoring saved instance state (in ");


        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Initialise theme
        //SharedPreferences sharedPreferences = getSharedPreferences("com.liftyourheads.dailyreadings", MODE_PRIVATE);
        paragraphs = sharedPreferences.getBoolean("pref_paragraphs", false);
        //sharedPreferences.edit().putString("translation", "ESV");
        curTranslation = sharedPreferences.getString("pref_translation", "NET"); //Default translation = ESV


        Log.i("Current Preferences","Paragraphs: " + paragraphs.toString() + ", Translation: " + curTranslation + ", Theme: " + sharedPreferences.getString("theme", "0"));

        Long[] time = new Long[7];

        time[0] = System.currentTimeMillis();

        //// DECLARE VIEWS ////

        dateFrameLayout = findViewById(R.id.dateFrameLayout);
        datesListView = findViewById(R.id.dateListView);
        readingNotesListView = findViewById(R.id.readingNotesListView);
        menuBackgroundConstraint = findViewById(R.id.menuBackgroundConstraint);
        readingsLayout = findViewById(R.id.readingsHeaderLayout);
        dateTextView = findViewById(R.id.dateTextView);
        firstReadingTextView = findViewById(R.id.firstReadingTextView);
        secondReadingTextView = findViewById(R.id.secondReadingTextView);
        thirdReadingTextView = findViewById(R.id.thirdReadingTextView);
        commentsViewPager = findViewById(R.id.commentsViewPager);
        commentsSwipeRefresh = findViewById(R.id.contentSwipeRefresh);
        commentsConnectErrorText = findViewById(R.id.commentsConnectErrorText);
        getCommentsProgressBar = findViewById(R.id.getCommentsProgressBar);
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fragmentManager = getSupportFragmentManager();

        time[1] = System.currentTimeMillis();
        Log.i("Startup Time", "Views declared in " + Long.toString(((time[1] - time[0]))) + " ms");


        //Initialise comments fragment

        time[2] = System.currentTimeMillis();
        Log.i("Startup Time", "Media declared in " + Long.toString(((time[2] - time[1]))) + " ms");

        ////////////////////////////

        time[3] = System.currentTimeMillis();
        Log.i("Startup Time", "Readings imported in " + Long.toString(((time[3] - time[2]))) + " ms");

        //Get current date (curDay, curMonth)
        getDate();
        selectedDay = curDay;
        selectedMonth = curMonth;

        time[4] = System.currentTimeMillis();
        Log.i("Startup Time", "Date retrieved in " + Long.toString(((time[4] - time[3]))) + " ms");

        //Fill date select list and set onClick functions
        populateList();

        time[5] = System.currentTimeMillis();
        Log.i("Startup Time", "List populated in " + Long.toString(((time[5] - time[4]))) + " ms");

        //Ensure DB are in correct location

        checkDatabase("DailyReadings.db");
        checkDatabase("BiblePlaces.db");
        for (String translation : TRANSLATIONS) {
            checkDatabase(translation + ".db");
        }

        rotateUI();

        //Set swipe refresh listener
        commentsSwipeRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {

                    @Override
                    public void onRefresh() {
                        Log.i("Reading Comments", "Refresh called from SwipeRefreshLayout");

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.

                        getComments(selectedDay,selectedMonth);

                    }

                }
        );

        //Set display to default (today's) readings
        setReadings(curDay,curMonth);

        //Update all comments
        //getAllComments();

        /*
        // Disable clicking while comments are still loading!
        firstReadingTextView.setClickable(false);
        secondReadingTextView.setClickable(false);
        thirdReadingTextView.setClickable(false);

        fragment_comments.getComments(selectedDay,selectedMonth);
        */

        if (!commentsDownloaded) {
            commentsViewPager.setVisibility(View.GONE);
        }

        time[6] = System.currentTimeMillis();
        Log.i("Startup Time", "Readings displayed in " + Long.toString(((time[6] - time[5]))) + " ms");

        Log.i("Total Startup Time",  Long.toString(((time[6] - time[0]))) + " ms");

    }

    public void downloadComments() {

        commentsSwipeRefresh.setRefreshing(false);

        if (!commentsDownloaded && !isRefreshing) {

            isRefreshing = true;

            commentsConnectErrorText.setVisibility(View.INVISIBLE);
            getCommentsProgressBar.setVisibility(View.VISIBLE);
            //readingNotesListView.setVisibility(View.INVISIBLE);

            getComments(selectedDay,selectedMonth);

        }

    }

    public void goToBibleReadings(View view) {

        Intent myIntent = new Intent(this, activity_readings.class);
        myIntent.putExtra("readingNum", Integer.parseInt((view.getTag()).toString()));
        startActivity(myIntent);
        overridePendingTransition(R.anim.slidein_right, R.anim.slideout_left);

    }

    public void goToSettings(View view) {

        Intent myIntent = new Intent(this, activity_settings.class);
        //myIntent.putExtra("readingNum", Integer.parseInt(view.getTag().toString()));
        startActivity(myIntent);
        //overridePendingTransition(R.anim.slidein_right, R.anim.slideout_left);

    }

    public void populateList() {

        nearbyReadingDates.clear();

        for (int i = -15; i < 15; i++) {

            Calendar calendar = Calendar.getInstance(); // this would default to now
            calendar.add(Calendar.DAY_OF_MONTH, i);

            int day = calendar.get(Calendar.DAY_OF_MONTH);
            String month = monthStringTemplate.format(calendar.getTime());

            if ((day == curDay) && (calendar.get(Calendar.MONTH) == curMonth)) {

                nearbyReadingDates.add("Yesterday");
                nearbyReadingDates.add("Today");
                nearbyReadingDates.add("Tomorrow");

                nearbyReadingDates.remove((i + 15 - 1));

                i += 1;

            } else {

                nearbyReadingDates.add(Integer.toString(day) + " " + month);

            }
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nearbyReadingDates);

        datesListView.setAdapter(arrayAdapter);

        datesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {



                // Reset viewPager
                commentsDownloaded = false;
                toggleViewPagerLoaded();

                String text = nearbyReadingDates.get(position);

                switch (text) {

                    case "Today":
                        selectedDay = curDay;
                        selectedMonth = curMonth;

                        break;

                    case "Tomorrow":

                        GregorianCalendar gc = new GregorianCalendar(TimeZone.getDefault());
                        gc.add(Calendar.DATE, 1);

                        selectedMonth = gc.get(Calendar.MONTH);
                        selectedDay = gc.get(Calendar.DAY_OF_MONTH);


                        break;

                    case "Yesterday":

                        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getDefault());
                        calendar.add(Calendar.DATE, -1);

                        selectedMonth = calendar.get(Calendar.MONTH);
                        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);


                        break;

                    default:

                        String[] date = nearbyReadingDates.get(position).split(" ");

                        selectedDay = Integer.parseInt(date[0]);
                        selectedMonth = Arrays.asList(MONTHS).indexOf(date[1]);

                        break;

                }

                Log.i("Date Selected", selectedDay.toString() + " " + selectedMonth.toString());

                if ((selectedMonth == 1) && (selectedDay == 29)) {

                    Toast.makeText(getApplicationContext(), "There aren't any readings set as this is a leap year!", Toast.LENGTH_LONG).show();
                    Log.i("Clicked List View", "No readings for this date! It's a Leap Year.");

                } else {

                    Log.i("Clicked List View", nearbyReadingDates.get(position));

                    setReadings(selectedDay, selectedMonth);

                    //Convert month name to number
                    /*
                    for (int i = 0; i < 12; i++) {

                        if (selectedMonth.equals(MONTHS[i])) {
                            selectedMonth = Integer.toString(i + 1);
                            break;
                        }

                    }*/

                    //Hide the date sidebar
                    toggleListView(null);

                }

            }
        });

        datesListView.setSelection(15);


    }


    public void toggleListView(View view) {

        System.out.println("Toggling Sidebar");
        if (listViewActive) {

            //If the list view is already open --> close it

            listViewActive = false;

            dateFrameLayout.animate().translationX(toPixels(-150)).setDuration(200);
            menuBackgroundConstraint.setVisibility(View.INVISIBLE);

        } else {

            //If the list view is closed --> Open it

            listViewActive = true;

            dateFrameLayout.animate().translationX(0).setDuration(200);
            menuBackgroundConstraint.setVisibility(View.VISIBLE);

        }

    }


    public void closeListView(View view) {

        if (listViewActive) {

            listViewActive = false;
            dateFrameLayout.animate().translationX(toPixels(-150)).setDuration(200);
            menuBackgroundConstraint.setVisibility(View.INVISIBLE);


        }

    }


    public void getDate() {

        //// GET CURRENT DATE INFO ////

        cal = new GregorianCalendar(TimeZone.getDefault());

        curMonth = cal.get(Calendar.MONTH);
        //curMonth = monthStringTemplate.format(cal.getTime());
        curDay = cal.get(Calendar.DAY_OF_MONTH);

    }

    /*public void getDateFromDatePicker(View view){
        DatePicker datePicker = findViewById(R.id.datePicker);

        int day = datePicker.getDayOfMonth();
        String month = MONTHS[datePicker.getMonth()];
        int year =  datePicker.getYear();

        getReadings(day, month, year);

        //Calendar calendar = Calendar.getInstance();
        //calendar.set(year, month, day);

        //return calendar.getTime();
    }*/


    public class IsServerReachable extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... urls) {

            Log.i("Network Status", "Starting");

            ConnectivityManager connMan = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connMan.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                try {

                    URL urlServer = new URL(urls[0]);
                    HttpURLConnection urlConn = (HttpURLConnection) urlServer.openConnection();
                    Log.i("Network Status", "Testing connection to " + urlServer);

                    urlConn.setConnectTimeout(3000); //<- 3Seconds Timeout
                    urlConn.connect();
                    Log.i("Network Status", Integer.toString(urlConn.getResponseCode()));
                    if (urlConn.getResponseCode() == 200) {
                        Log.i("Network Status", "Online");
                        return true;

                    } else {
                        Log.i("Network Status", "Offline");

                        return false;
                    }
                } catch (MalformedURLException e1) {
                    return false;
                } catch (IOException e) {
                    return false;
                }
            }

            Log.i("Network Status", "Offline");

            return false;

        }
    }

    public int toPixels(int dp) {

        float d = getApplicationContext().getResources().getDisplayMetrics().density;
        return (int)(dp * d); // margin in pixels

    }

    public class Reading {

        private Integer number;
        private StringBuilder fullName = new StringBuilder();
        private String[] bookName;
        private Integer[] bookPosition;
        private Integer[] chapters = null;
        private Integer numChapters;
        private int[] chapterStartCounters;
        private Boolean chapterPartial = false;
        private Boolean singleChapterBook = false;
        private Boolean multipleBooks = false;
        private String[][][] verses;
        private ArrayList<HashMap<String, String>> verseList;
        private SpannableStringBuilder verseParagraphs = new SpannableStringBuilder();
        private String[] audioURL;
        private Integer[] audioStreamStatus;
        private Integer[] versesPartialChapter = new Integer[2];
        private List<String[]> places;


        private void setNumber(int value) {
            this.number = value;
        }

        private Boolean isChapterPartial() {
            return this.chapterPartial;
        }

        public Boolean isMultipleBooks() {
            return this.multipleBooks;
        }

        private void initialize(String[][] portions) {

            setBookName(portions[(number-1)][0]);
            setChapters(portions[(number-1)][1]);
            setFullName();
            generateAudioURL();
            updateVerses();
            processPlaces();

        }

        private void setBookName(String name) {

            if (!name.contains(",")) {

                this.bookName = new String[1];
                this.bookName[0] = name;

            } else { // Multiple books included! In this case, 2 + 3 Jn

                this.bookName = new String[2];
                this.bookName = name.split(", ");
                this.multipleBooks = true;

            }

            if (!this.multipleBooks) {

                this.bookPosition = new Integer[1];
                this.bookPosition[0] = Arrays.asList(BIBLE).indexOf(this.bookName[0]);

            } else {

                this.bookPosition = new Integer[2];

                for (int i = 0; i < this.bookName.length; i++) {

                    this.bookPosition[i] = Arrays.asList(BIBLE).indexOf(this.bookName[i]);

                }

            }
        }

        public void processPlaces() {

            // Get list of places by chapter from DB and
            SQLiteDatabase readingsDB = getApplicationContext().openOrCreateDatabase("BiblePlaces.db", 0, null);

            //Establishing variables
            Integer[] bookPosition = this.getBookIndex();
            Integer[] bookChapters = this.getChapters();
            Cursor readingCursor,placesCursor;
            places = new ArrayList<>();

            //Iterating through each chapter in reading
            for (int i = 0; i < bookChapters.length; i++ ) {

                String readingBook = BIBLE_PLACES[bookPosition[0]];
                Integer readingChapter = bookChapters[i];

                // Find places in DB
                readingCursor = readingsDB.rawQuery("SELECT * FROM bible_places_CHAPTER WHERE book = '" + readingBook + "\' AND chapter = " + readingChapter.toString(), null);

                Log.i("Map data Found", "Found map data for " + readingBook + " chapter " + readingChapter.toString());



                if (readingCursor.moveToFirst()) {

                    int placesColumn = readingCursor.getColumnIndex("places");

                    do {

                        String placesList = readingCursor.getString(placesColumn);
                        String[] placesArray = placesList.split(", ");

                        for (String placeName : placesArray) {

                            placesCursor = readingsDB.rawQuery("SELECT * FROM bible_places_NAME WHERE Name = '" + placeName + "\'", null);
                            Integer latColumn = placesCursor.getColumnIndex("Lat");

                            if (placesCursor.moveToFirst()) {
                                do {
                                    String placeLat = placesCursor.getString(latColumn);
                                    String placeLong = placesCursor.getString(latColumn + 1);

                                    String[] placeData = new String[]{placeName, placeLat, placeLong};

                                    Log.i("Place Info", Arrays.toString(placeData));
                                    places.add(placeData);

                                } while (placesCursor.moveToNext());

                            } else {
                                Log.i("Place Info", "Couldn't find '" + placeName + "' in the database!");
                            }

                            placesCursor.close();

                        }

                    } while (readingCursor.moveToNext());
                } else {
                    Log.i("Place Info",readingBook + " " + readingChapter + " has no recorded places");
                }

                readingCursor.close();

                System.out.println("Place Data: " + Arrays.toString(places.toArray()));
            }

            readingsDB.close();
        }

        public List<String[]> getPlaces() {
            return places;
        }

        private void setChapters(String chapters) {
            String[] chaptersSplit;

            if (chapters.contains(":")) {

                //If chapter is partial
                chapterPartial = true;
                chaptersSplit = chapters.split(":");

                //Only one chapter
                this.numChapters = 1;
                this.chapters = new Integer[numChapters];
                this.chapters[0] = Integer.parseInt(chaptersSplit[0]);

                //Split start & end verses
                String[] versesPartial = chaptersSplit[1].split("-");

                this.versesPartialChapter[0] = Integer.parseInt(versesPartial[0]);
                this.versesPartialChapter[1] = Integer.parseInt(versesPartial[1]);

            } else {

                //If chapter is not partial

                if (chapters.contains("-")) {

                    // More than two chapters
                    chaptersSplit = chapters.split("-");
                    this.numChapters = Integer.parseInt(chaptersSplit[1]) - Integer.parseInt(chaptersSplit[0]) + 1; // +1 accounts for initial chapter ie. 45-43 = 2 ( +1 for starting chapter = 3)
                    this.chapters = new Integer[numChapters];

                    this.chapters[0] = Integer.parseInt(chaptersSplit[0]);

                    for (int i = 1; i < numChapters; i++) {

                        this.chapters[i] = this.chapters[(i-1)] + 1;

                    }


                } else if (chapters.contains(",")) {

                    //Two chapters
                    chaptersSplit = chapters.split(",");
                    this.numChapters = chaptersSplit.length;
                    this.chapters = new Integer[numChapters];


                    for (int i = 0; i < numChapters; i++) {

                        this.chapters[i] = Integer.parseInt(chaptersSplit[i]);

                    }


                } else {


                    if (!chapters.equals("")) {
                        //Only one chapter
                        this.numChapters = 1;
                        this.chapters = new Integer[numChapters];

                        this.chapters[0] = Integer.parseInt(chapters);

                    } else if (!multipleBooks) {

                        //Only one chapter
                        this.numChapters = 1;
                        this.chapters = new Integer[numChapters];

                        this.singleChapterBook = true;
                        this.chapters[0] = 1;
                    } else { //Only true for 2 + 3 Jn

                        //Two chapters
                        this.numChapters = 2;
                        this.chapters = new Integer[numChapters];

                        //Always first chapter of book
                        this.chapters[0] = 1;
                        this.chapters[1] = 1;

                    }

                }

            }

        }

        public Integer[] getBookIndex() {

            return this.bookPosition;

        }

        private void setFullName() {

            if (!this.singleChapterBook && !this.multipleBooks) { //Include chapter number if book contains more than one!

                this.fullName.append(this.bookName[0]).append(" ").append(chapters[0]);
                System.out.println("Reading " + number + " contains only one book!");

            } else if (this.multipleBooks) {

                this.fullName.append(this.bookName[0]).append(", ").append(this.bookName[1]);

                System.out.println("Reading " + number + " contains multiple books!");

            } else if (this.singleChapterBook) {

                this.fullName.append(this.bookName[0]);
                System.out.println("Reading " + number + " contains only one chapter in the whole book!");


            }


            //Append extra chapters
            if (chapters.length > 2) {

                // Add final chapter
                this.fullName.append("-").append(chapters[(chapters.length-1)]); //Add last chapter

            } else if (chapters.length == 2 && !multipleBooks) {

                //Add second chapter
                this.fullName.append(", ").append(chapters[1]);

            } else if (this.isChapterPartial()) {

                //Add verses range
                this.fullName.append(" : ").append(this.versesPartialChapter[0]).append(" - ").append(this.versesPartialChapter[1]);

            }

        }

        public void generateAudioURL() {

            this.audioURL = new String[numChapters];
            this.audioStreamStatus = new Integer[numChapters];

            for (int i = 0; i < chapters.length; i++) {

                PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                curTranslation = sharedPreferences.getString("pref_translation", "ESV"); //Default translation = ESV

                //Set audio URL
                switch (curTranslation) {

                    case "NET":
                        //TODO: Get correct NET audio url!

                        //TODO: Fix audio urls for multiple books

                        this.audioURL[i] = "http://feeds.bible.org/netaudio/" + (bookPosition[0] + 1) + "-" + bookName[0].replace(" ", "") + "-" + String.format(Locale.getDefault(),"%02d", chapters[i]) + ".mp3";
                        break;

                    //Use KJV for default readings
                    default:
                        this.audioURL[i] = "http://server.firefighters.org/kjv/projects/firefighters/kjv_web/" + String.format(Locale.getDefault(),"%02d", (bookPosition[0] + 1)) + "_" + bookName[0].replace(" ", "").substring(0, 3) + "/" + String.format(Locale.getDefault(),"%02d", (bookPosition[0] + 1)) + bookName[0].replace(" ", "").substring(0, 3) + String.format(Locale.getDefault(),"%03d", chapters[i]) + ".mp3";
                        break;

                }


            }

            //Todo: Reduce startup impact
            //checkAudioURLStream();


        }

        public void checkAudioURLStream() {


            for (int i = 0; i < this.numChapters; i++) {

                try {

                    this.audioStreamStatus[i] = new DoesFileExist().execute(this.audioURL[i]).get();
                    Log.i("Does File Exist", "URL: " + this.audioURL[i] + "  Response Code: " + this.audioStreamStatus[i].toString());

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }

        }

        public void updateVerses() {

            //Update reading verses
            verseList = new ArrayList<>();

            String verseNumber = null;
            String verseContent;
            //JSONObject readingChapterCurrent = null;
            chapterStartCounters = new int[chapters.length];

            Log.i("Getting Verses", Arrays.asList(this.bookName) + " " + Arrays.asList(this.chapters));

            verses = getVerses(this.bookName, this.chapters, this.isChapterPartial(), this.versesPartialChapter);

            HashMap<String, String> verse;

            Integer curVerse;

            if (this.chapterPartial) {

                //Change starting verse number
                curVerse = versesPartialChapter[0];

            } else {

                curVerse = 1;

            }

            for (int book = 0; book < this.bookName.length; book++) {
                for (Integer i = 0; i < verses[book].length; i++) {

                    chapterStartCounters[i] = verses[book][i].length;

                    //Set starting verse to 0
                    if (i != 0) curVerse = 1;

                    // Add chapter title to start
                    if (this.chapters.length > 1) {

                        if (i != 0) verseParagraphs.append("<br/><br/>");
                        verseParagraphs.append("<big><span style=\"color:#9ccc65\"><b>" + this.bookName[book] + " " + this.chapters[i] + "</b></span></big><br/>");
                    }

                    try {

                        for (int j = 0; j < verses[book][i].length; j++) {

                            //Skip verse if it is blank (in case of modern translations)
                            if (verses[book][i][j].equals("")) continue;

                            //For list view
                            verse = new HashMap<>();

                            if (j != 0) {

                                if (verses[book][i][j].contains("<pb/>")) {

                                    verseParagraphs.append("<br/>");

                                } else {

                                    verseParagraphs.append("&nbsp;");

                                }
                            }

                            verseNumber = Integer.toString((j+1));
                            verseContent = verses[book][i][j].replaceAll("([A-Z][\\w'-]*(?:\\s+[A-Z][\\w'-]*|\\s+(?:a|an|for|the|and|but|or|on|in|with))+(?: <br\\/>)+(?:\\s*))", "").replaceAll("<[^>]*>", "").trim();//.replaceAll("(\\{[a-zA-Z0-9.,: ;]+\\})+$", "").replaceAll("&#x27;", "'").replaceAll("\\{(.*?)\\}", "$1");

                            verse.put("verseNum", curVerse.toString());
                            verse.put("verseContent", verseContent);
                            verseList.add(verse);

                            //For paragraph view
                            verseParagraphs.append("<span style=\"color:#9ccc65\"><small><sup><b><i>").append(curVerse.toString()).append("</i></b></sup></small></span>&nbsp;").append(verses[book][i][j].replaceAll("<pb/>", ""));//

                            curVerse++;
                        }
                        //chapterStartCounters[i]++;

                    } catch (Exception e) {

                        e.printStackTrace();
                        Log.i("Read Error", "Error reading verse " + verseNumber);

                    }
                }
            }


            for (int i = 1; i<(chapters.length); i++) {

                chapterStartCounters[i] += chapterStartCounters[(i-1)];

            }

            Log.i("Counter values", Arrays.toString(chapterStartCounters));


        }

        public String getAudioURL(int i) {
            return audioURL[i];
        }

        public String[] getAudioURLS() {
            return audioURL;
        }

        public Integer getAudioStreamStatus(int i) {
            return audioStreamStatus[i];
        }

        public String getFullName() {
            return fullName.toString();
        }

        public Integer getNumChapters() {
            return numChapters;
        }

        public int getChapterStartCounters(int i) {
            return chapterStartCounters[i];
        }

        public String getBookName(String bookNumber) {

            StringBuilder name = new StringBuilder();

            if (bookNumber == null) {

                for (int i = 0; i < bookName.length; i++) {
                    name.append(bookName[i]);
                }

            } else {

                name.append(bookName[Integer.parseInt(bookNumber)]);

            }

            return name.toString();
        }

        public Integer[] getBookPosition() {
            return bookPosition;
        }

        public Integer[] getChapters() {
            return chapters;
        }

        public Integer getChapter(int i) {
            return chapters[i];
        }

        public ArrayList<HashMap<String, String>> getReadingVerses() {

            return verseList;

        }

        public String getVerseParagraphs() {

            return verseParagraphs.toString();

        }


    }

    public static void checkDatabase(String dbName) {

        DataBaseHelper myDbHelper;
        myDbHelper = new DataBaseHelper(CONTEXT, dbName);
        //myDbHelper.setDatabaseName(dataBase);

        try {

            //myDbHelper.db_delete();
            myDbHelper.createDatabase();

        } catch (IOException ioe) {

            throw new Error("Unable to create database");

        }

    }

    public void setReadings(Integer day, Integer month){

        if (month == 1 && day == 29 ) {

            //No readings set for this day!
            Log.i("Leap Year", "No readings set!");
            Toast.makeText(this, "There aren't any readings set as this is a leap year!", Toast.LENGTH_LONG).show();
            toggleListView(null);

        } else {

            readings = new Reading[3];
            String[][] portions = getDailyReadings(day, month);

            for (int i = 0; i < 3; i++) {
                readings[i] = new Reading();
                readings[i].setNumber((i + 1));
                readings[i].initialize(portions);
            }

            // Set title to selected date
            dateTextView.setText(Integer.toString(day) + " " + MONTHS[month]);

            // Set readings text view to selected date's readings
            firstReadingTextView.setText(readings[0].getFullName());
            secondReadingTextView.setText(readings[1].getFullName());
            thirdReadingTextView.setText(readings[2].getFullName());


            getComments(day, month);

            Log.i("Date", Integer.toString(day) + " " + (month + 1));
            Log.i("Days Readings", readings[0].getFullName() + ", " + readings[1].getFullName() + ", " + readings[2].getFullName());

        }

    }

    public String[][] getDailyReadings(Integer day, Integer month) {

            SQLiteDatabase readingsDB = this.openOrCreateDatabase("DailyReadings.db", MODE_PRIVATE, null);

            Cursor reading = readingsDB.rawQuery("SELECT * FROM daily_readings WHERE month = '" + MONTHS[month] + "\' AND date = " + day.toString(), null);

            Log.i("Readings Found", Integer.toString(reading.getCount()));

            reading.moveToFirst();
            String[][] portions = new String[3][2];

            int portionColumn = reading.getColumnIndex("first_book");

            do {

                for (int i = 0; i < 3; i++) {
                    portions[i][0] = reading.getString(portionColumn++);
                    portions[i][1] = reading.getString(portionColumn++);
                }

            } while (reading.moveToNext());

            System.out.println("Portion 1: " + Arrays.toString(portions[0]));
            System.out.println("Portion 2: " + Arrays.toString(portions[1]));
            System.out.println("Portion 3: " + Arrays.toString(portions[2]));

            reading.close();
            readingsDB.close();
            return portions;

    }

    public String[][][] getVerses(String[] bookName, Integer[] chapters, Boolean isPartialChapter, Integer[] versesPartial) {

        //Open the db for current translation
        SQLiteDatabase bibleDB = this.openOrCreateDatabase(curTranslation + ".db", MODE_PRIVATE, null);
        Cursor reading = null;

        //Initialise verses array based on number of chapters
        String[][][] verses = new String[bookName.length][chapters.length][];

        int counter = 0;

        try {
            for (String book : bookName) {

                //Get the current book number for use in verses db
                Cursor bookCur = bibleDB.rawQuery("SELECT * FROM books WHERE long_name = \'" + book + "\'", null);

                bookCur.moveToFirst();
                String bookNumber = null;

                try {

                    bookNumber = bookCur.getString(bookCur.getColumnIndex("book_number"));
                    bookCur.close();

                } catch (Exception e) {

                    e.printStackTrace();
                    Log.i("Getting Book Number", "Invalid book name: " + book);
                }


                    //Iterate through each chapter and add to array
                    for (int i = 0; i < chapters.length; i++) {

                        //Customise query to match current chapter
                        if (!isPartialChapter) {

                            reading = bibleDB.rawQuery("SELECT * FROM verses WHERE book_number = " + bookNumber + " AND chapter = " + chapters[i].toString(), null);

                        } else {

                            reading = bibleDB.rawQuery("SELECT * FROM verses WHERE book_number = " + bookNumber + " AND chapter = " + chapters[i].toString() + " AND verse BETWEEN " + versesPartial[0] + " AND " + versesPartial[1], null);

                        }

                        //Define size of array for this chapter
                        verses[counter][i] = new String[reading.getCount()];

                        int contentIndex = reading.getColumnIndex("text");

                        //Log.i("# Results", Integer.toString(reading.getCount()));

                        reading.moveToFirst();
                        int j = 0;

                        do {

                            verses[counter][i][j] = reading.getString(contentIndex).replaceAll("’", "'").replaceAll("<f>.*?</f>", "");//.replaceAll("<pb/>", "")
                            j++;

                        } while (reading.moveToNext());

                    }

                counter++;

            }

        } finally {

            reading.close();
            bibleDB.close();
        }

        return verses;

    }



    public static class CommentsPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 3;

        public CommentsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages.
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for a particular page.
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return fragment_comments.newInstance(1);
                case 1:
                    return fragment_comments.newInstance(2);
                case 2:
                    return fragment_comments.newInstance(3);
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Tab " + position;
        }

    }

    //Single use (update database)
    public void getAllComments() {

        if (Build.VERSION.SDK_INT > 25) {
            Log.i("Comments", "Downloading ALL COMMENTS. Please be patient");

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            DateTimeFormatter monthFormat = DateTimeFormatter.ofPattern("MM");

            try {
                Date startDate = formatter.parse("2018-01-01");
                Date endDate = formatter.parse("2018-01-31");

                LocalDate start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                SQLiteDatabase commentsDB = this.openOrCreateDatabase("DailyReadings.db", MODE_PRIVATE, null);

                //commentsDB.delete("comments_new",null,null);

                for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                    // Do your job here with `date`.
                    System.out.println(date);

                    int month = Integer.parseInt(date.format(monthFormat)) - 1;
                    int day = date.getDayOfMonth();

                    /*
                    //Set url based on selection
                    String commentsSite = "https://www.dailyreadings.org.uk/default.asp?act=notesdisplay&lay=3&m=" + month + "&d=" + day;

                    //Start download of website HTML

                    DownloadComments task = new DownloadComments();
                    try {
                        saveCommentsToDB((month-1),day,task.execute(commentsSite).get().split("<div class=\"readings_header\">"));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    */


                    Cursor reading = commentsDB.rawQuery("SELECT * FROM comments WHERE month = '" + MONTHS[month] + "\' AND date = " + day, null);

                    Log.i("Comments Found", Integer.toString(reading.getCount()));

                    reading.moveToFirst();

                    String[] commentChunks = new String[3];
                    int curColumn = reading.getColumnIndex("first");

                    for (int i = 0; i < 3; i++) {   //Iterate through 3 readings

                        commentChunks[i] = reading.getString(curColumn).replaceAll("&nbsp;"," ");
                        curColumn++;
                    }

                    reading.close();

                    try {

                        allComments = new String[commentChunks.length][];

                        notesSize = new int[commentChunks.length];
                        maxNotes = 0;


                        /// DIVIDE READINGS INTO INDIVIDUAL NOTES ///

                        for (int i = 0; i < commentChunks.length; i++) {

                            allComments[i] = commentChunks[i].split("<div class=\"note\">");

                            /// DETERMINE ARRAY SIZE FOR COMMENT ARRAY BELOW ///
                            //if (allComments[i].length > maxNotes) {

                            notesSize[i] = allComments[i].length;

                            if (notesSize[i] > maxNotes) {

                                maxNotes = notesSize[i];

                            }

                        }


                        Log.i("Maximum notes size", java.util.Arrays.toString(notesSize));

                        /// SEPARATE NOTES INTO CONTENT AND POSTER ///

                        commentsDownloaded = true;

                        //separateNoteFromPoster(commentChunks.length);
                        //initialiseNotesListView();

                        //updateNotesListView(1);

                        //readingNotesListView.setVisibility(View.VISIBLE);


                    } catch (NullPointerException e) {


                        commentsConnectErrorText.setVisibility(View.VISIBLE);

                        isRefreshing = false;

                        e.printStackTrace();

                    } catch (Exception e) {

                        e.printStackTrace();

                    }

                    //Separate comment from poster
                    commentPost = new String[commentChunks.length][maxNotes][];

                    for (int i = 0; i < commentChunks.length; i++) {

                        for (int i1 = 0; i1 < allComments[i].length; i1++) {

                            //Divide allComments into comment and poster info
                            commentPost[i][i1] = allComments[i][i1].split("<p class=\"user\">");

                        }
                    }

                    //Log.i("Comments size", commentPost[2][1][1]);
                    //Log.i("Comments", java.util.Arrays.toString(commentPost[1][46]));



                    //Output final data

                    for (int readingCur = 0; readingCur < commentChunks.length; readingCur++) {


                        for (int note = 1; note < notesSize[readingCur]; note++) {

                            //Log.i("Notes","Note " + String.valueOf(note));
                            //if(readingCur == 2 && note == (commentPost[readingCur].length - 1)) break;
                            ContentValues comment = new ContentValues();
                            comment.put("month", MONTHS[month]);
                            comment.put("day", day);
                            comment.put("reading", (readingCur + 1));
                            comment.put("comment_num", note);
                            //comment.put("comment_body", commentPost[readingCur][note][0]);
                            //comment.put("comment_poster", commentPost[readingCur][note][1]);


                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                //Code for Nougat+

                                comment.put("comment_body", Html.fromHtml(commentPost[readingCur][note][0].replaceAll("<p style=\"color:#([a-zA-z0-9]+)\">\n" + "\t&nbsp;</p>\n", "").replaceAll("<div>\n" +
                                        "\t&nbsp;</div>", ""), Html.FROM_HTML_MODE_LEGACY).toString().replaceAll("[\\n]{3,}", "\n\n").trim().concat("\n").replaceAll("[\\r]{3,}", "\n\n").replaceAll("/t",""));
                                comment.put("comment_poster", Html.fromHtml(commentPost[readingCur][note][1], Html.FROM_HTML_MODE_LEGACY).toString().replaceAll("Reply to ([a-zA-Z]+)", "").replace(" Comment added in",","));//.split("Comment added in"));

                            } else {
                                //Code for older OS

                                comment.put("comment_body", Html.fromHtml(commentPost[readingCur][note][0].replaceAll("<p style=\"color:#([a-zA-z0-9]+)\">\n" + "\t&nbsp;</p>\n", "").replaceAll("<div>\n" +
                                        "\t&nbsp;</div>", "")).toString().replaceAll("[\\n]{3,}", "\n\n").trim().concat("\n").replaceAll("[\\r]{3,}", "\n\n").replaceAll("/t",""));
                                comment.put("comment_poster", Html.fromHtml(commentPost[readingCur][note][1]).toString().replaceAll("Reply to ([a-zA-Z]+)", "").replace(" Comment added in",","));//.split("Comment added in"));

                            }
                            commentsDB.insert("comments_new", null, comment);

                        }

                        Log.i("Comments Output", "Comments completed for reading " + (readingCur + 1));

                    }

                    Log.i("Comments","Comments updated for " + MONTHS[month] + " " + day);



                }

                commentsDB.close();


            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }

    public void getComments(Integer day, Integer month) {

        String[] comments = new String[4];
        Boolean isCommentsInDatabase;

        // Disable clicking while comments are still loading!

        firstReadingTextView.setClickable(false);
        secondReadingTextView.setClickable(false);
        thirdReadingTextView.setClickable(false);


        commentsSwipeRefresh.setRefreshing(false);

        if (!commentsDownloaded && !isRefreshing) {

            isRefreshing = true;

            commentsConnectErrorText.setVisibility(View.INVISIBLE);
            getCommentsProgressBar.setVisibility(View.VISIBLE);
            //readingNotesListView.setVisibility(View.INVISIBLE);

            // Get date info to use (selected from populateList)

            if (day == null || day < 1 || day > 31) {

                day = curDay;

            }

            if (month == null) {

                month = curMonth;

            }

            //Todo: Check database for comments

            SQLiteDatabase commentsDB = this.openOrCreateDatabase("DailyReadings.db", MODE_PRIVATE, null);

            Cursor reading = commentsDB.rawQuery("SELECT * FROM comments WHERE month = '" + MONTHS[month] + "\' AND date = " + day.toString(), null);

            Log.i("Comments Found", Integer.toString(reading.getCount()));

            reading.moveToFirst();

            isCommentsInDatabase = Boolean.valueOf(reading.getString(reading.getColumnIndex("downloaded")));

            if (isCommentsInDatabase) {

                Log.i("Comments", "Comments found in DB");

                comments[0] = ""; //Empty first cell to match URL download split

                int curColumn = reading.getColumnIndex("first");

                for (int i = 0; i < 3; i++) {

                    comments[(i + 1)] = reading.getString(curColumn);
                    curColumn++;
                }

            }

            reading.close();
            commentsDB.close();

            if (!isCommentsInDatabase) {

                Log.i("Comments", "Unable to find comments in DB!");

                //Set url based on selection
                String commentsSite = "https://www.dailyreadings.org.uk/default.asp?act=notesdisplay&lay=3&m=" + (month + 1) + "&d=" + day;

                //Start download of website HTML

                DownloadComments task = new DownloadComments();
                task.execute(commentsSite);

                //updateNotesListView(1);

            } else {

                processCommentData(comments);

                commentsSwipeRefresh.setRefreshing(false);

            }

            //getComments(selectedDay,selectedMonth);

        }
    }


    public class DownloadComments extends AsyncTask<String, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            getCommentsProgressBar.setVisibility(View.VISIBLE);
            //readingNotesListView.setVisibility(View.INVISIBLE);

//            firstReadingTextView.setClickable(false);
//            secondReadingTextView.setClickable(false);
//            thirdReadingTextView.setClickable(false);

        }

        @Override
        protected String doInBackground(String... urls) {

            URL url;

            try {

                url = new URL(urls[0]);

                Log.i("Starting download", urls[0]);

                BufferedReader r = new BufferedReader(
                        new InputStreamReader(url.openStream()));

                StringBuilder total = new StringBuilder(url.openStream().available());
                String line;

                while ((line = r.readLine()) != null) {

                    total.append(line).append('\n');

                }

                r.close();

                Log.i("Completed download", urls[0]);

                return total.toString();

            } catch (MalformedURLException e) {

                e.printStackTrace();

            } catch (UnknownHostException e) {

                Log.e("FileDownload", "Failed! No connection to the server. Check internet connection.");

            } catch (IOException e) {

                e.printStackTrace();

            }
            return null;

        }

        @Override
        protected void onPostExecute(String commentsHTML) {
            super.onPostExecute(commentsHTML);


            //Get comments for 3 readings
            readingChunks = commentsHTML.split("<div class=\"readings_header\">");

            ///Send comments to DB

            Log.i("Comments","Saving comments for date: " + MONTHS[selectedMonth] + " " + selectedDay.toString());
            saveCommentsToDB(selectedMonth,selectedDay,readingChunks);

            //processCommentData(readingChunks);

            firstReadingTextView.setClickable(true);
            secondReadingTextView.setClickable(true);
            thirdReadingTextView.setClickable(true);

        }

    }

    public void processCommentData(String[] commentChunks) {

        try {

            allComments = new String[commentChunks.length][];

            notesSize = new int[commentChunks.length];
            maxNotes = 0;


            /// DIVIDE READINGS INTO INDIVIDUAL NOTES ///

            for (int i = 0; i < commentChunks.length; i++) {

                allComments[i] = commentChunks[i].split("<div class=\"note\">");

                /// DETERMINE ARRAY SIZE FOR COMMENT ARRAY BELOW ///
                //if (allComments[i].length > maxNotes) {

                notesSize[i] = allComments[i].length;

                if (notesSize[i] > maxNotes) {

                    maxNotes = notesSize[i];

                }

            }


            Log.i("Maximum notes size", java.util.Arrays.toString(notesSize));

            /// SEPARATE NOTES INTO CONTENT AND POSTER ///

            commentsDownloaded = true;

            separateNoteFromPoster(commentChunks.length);
            initialiseNotesListView();

            //updateNotesListView(1);

            //readingNotesListView.setVisibility(View.VISIBLE);


        } catch (NullPointerException e) {


            commentsConnectErrorText.setVisibility(View.VISIBLE);

            isRefreshing = false;

            e.printStackTrace();

        } catch (Exception e) {

            e.printStackTrace();

        }

        getCommentsProgressBar.setVisibility(View.INVISIBLE);


    }

    public void saveCommentsToDB(Integer month, Integer day, String[] comments) {

        //TODO: Save comments to database for future access

        SQLiteDatabase commentsDB = this.openOrCreateDatabase("DailyReadings.db", MODE_PRIVATE, null);

        Log.i("Comments","Database opened");

        String curDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        //Info for entry
        ContentValues contentValues = new ContentValues();
        contentValues.put("downloaded","true");
        contentValues.put("downloaded_date", curDate);
        contentValues.put("first",comments[1]);
        contentValues.put("second",comments[2]);
        contentValues.put("third",comments[3]);

        Log.i("Comments","Content values filled");

        //Update data for selected date
        commentsDB.update("comments", contentValues, "month=? AND date=?", new String[] {MONTHS[month],day.toString()});

        Log.i("Comments","Updated comments for " + MONTHS[month] + " " + day.toString());

        commentsDB.close();

    }

    public void initialiseNotesListView() {

        commentList = new ArrayList<>();
        ArrayList<HashMap<String, String>> readingCommentList = new ArrayList<>();

        HashMap<String, String> item;

        for (int reading = 1; reading < 4; reading++) {


            if (commentPost != null) {
                for (int i = 1; i < commentPost[reading].length; i++) {

                    // Check if current position is less than total number of comments for the specified reading
                    if (i < notesSize[(reading)]) {
                        try {

                            item = new HashMap<>();
                            String post;
                            String[] poster;

                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                //Code for Nougat+

                                //// STRIP UNNECESSARY TEXT ////
                                post = Html.fromHtml(commentPost[reading][i][0].replaceAll("<p style=\"color:#([a-zA-z0-9]+)\">\n" + "\t&nbsp;</p>\n", "").replaceAll("<div>\n" +
                                        "\t&nbsp;</div>", ""), Html.FROM_HTML_MODE_LEGACY).toString().replaceAll("[\\n]{3,}", "\n\n").trim().concat("\n").replaceAll("[\\r]{3,}", "\n\n");
                                poster = Html.fromHtml(commentPost[reading][i][1], Html.FROM_HTML_MODE_LEGACY).toString().replaceAll("Reply to ([a-zA-Z]+)", "").split("Comment added in");


                            } else {
                                //Code for older OS

                                //// REMOVE UNNECESSARY TEXT ////
                                post = Html.fromHtml(commentPost[reading][i][0].replaceAll("<p style=\"color:#([a-zA-z0-9]+)\">\n" + "\t&nbsp;</p>\n", "").replaceAll("<div>\n" +
                                        "\t&nbsp;</div>", "")).toString().replaceAll("[\\n]{3,}", "\n\n").trim().concat("\n").replaceAll("[\\r]{3,}", "\n\n");
                                poster = Html.fromHtml(commentPost[reading][i][1]).toString().replaceAll("Reply to ([a-zA-Z]+)", "").split("Comment added in");

                            }

                            item.put("comment", post);
                            item.put("poster", poster[0].replaceFirst("\\s++$", "") + ", " + poster[1]); // Remove trailing whitespace
                            readingCommentList.add(item);

                        } catch (Exception e) {

                            e.printStackTrace();
                            Log.i("Current iteration", Integer.toString(i));
                            break;

                        }
                    }

                }
            }

            commentList.add(new ArrayList<>(readingCommentList));
            readingCommentList.clear();

        }

        //Initialise the viewpager
        adapterViewPager = new CommentsPagerAdapter(getSupportFragmentManager());
        commentsViewPager.setAdapter(adapterViewPager);

        commentsViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                setTitleColours(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

        });

        setTitleColours(0);

        toggleViewPagerLoaded();
        //readingNotesListView.setAdapter(commentsSA);


        // Re-enable clicking now that comments are loaded!!
        firstReadingTextView.setClickable(true);
        secondReadingTextView.setClickable(true);
        thirdReadingTextView.setClickable(true);


        if (isRefreshing) {

            commentsSwipeRefresh.setRefreshing(false);
            isRefreshing = false;

        }

    }

    public void separateNoteFromPoster(int commentChunksLength) {

        //Todo: Make more efficient array size
        commentPost = new String[commentChunksLength][maxNotes][];

        for (int i = 0; i < commentChunksLength; i++) {

            for (int i1 = 0; i1 < allComments[i].length; i1++) {

                //Divide allComments into comment and poster info
                commentPost[i][i1] = allComments[i][i1].split("<p class=\"user\">");

            }
        }

    }

    public void setTitleColours(int reading) {

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.colorBackgroundHighlight, typedValue, true);
        @ColorInt int colorBackgroundHighlight = typedValue.data;
        theme.resolveAttribute(R.attr.colorBackgroundLight, typedValue, true);
        @ColorInt int colorBackgroundLight = typedValue.data;
        theme.resolveAttribute(R.attr.colorHighlightText, typedValue, true);
        @ColorInt int colorHighlightText = typedValue.data;
        theme.resolveAttribute(R.attr.colorReadingText, typedValue, true);
        @ColorInt int colorReadingText = typedValue.data;


        switch (reading) {

            case 0:
                findViewById(R.id.firstReadingLinearLayout).setBackgroundColor(colorBackgroundHighlight);
                findViewById(R.id.secondReadingLinearLayout).setBackgroundColor(colorBackgroundLight);
                findViewById(R.id.thirdReadingLinearLayout).setBackgroundColor(colorBackgroundLight);

                ((TextView)findViewById(R.id.firstReadingTextView)).setTextColor(colorHighlightText);
                ((TextView)findViewById(R.id.secondReadingTextView)).setTextColor(colorReadingText);
                ((TextView)findViewById(R.id.thirdReadingTextView)).setTextColor(colorReadingText);

                break;

            case 1:
                findViewById(R.id.firstReadingLinearLayout).setBackgroundColor(colorBackgroundLight);
                findViewById(R.id.secondReadingLinearLayout).setBackgroundColor(colorBackgroundHighlight);
                findViewById(R.id.thirdReadingLinearLayout).setBackgroundColor(colorBackgroundLight);

                ((TextView)findViewById(R.id.firstReadingTextView)).setTextColor(colorReadingText);
                ((TextView)findViewById(R.id.secondReadingTextView)).setTextColor(colorHighlightText);
                ((TextView)findViewById(R.id.thirdReadingTextView)).setTextColor(colorReadingText);

                break;

            case 2:
                findViewById(R.id.firstReadingLinearLayout).setBackgroundColor(colorBackgroundLight);
                findViewById(R.id.secondReadingLinearLayout).setBackgroundColor(colorBackgroundLight);
                findViewById(R.id.thirdReadingLinearLayout).setBackgroundColor(colorBackgroundHighlight);

                ((TextView)findViewById(R.id.firstReadingTextView)).setTextColor(colorReadingText);
                ((TextView)findViewById(R.id.secondReadingTextView)).setTextColor(colorReadingText);
                ((TextView)findViewById(R.id.thirdReadingTextView)).setTextColor(colorHighlightText);

                break;

        }

    }

    /*public void updateNotesListView(int reading) {

        ArrayList<HashMap<String, String>> commentList = new ArrayList<>();

        commentsSA = new SimpleAdapter(getActivity(), commentList,
                R.layout.note,
                new String[]{"comment", "poster"},
                new int[]{R.id.line_a, R.id.line_b});

        HashMap<String, String> item;

        if (commentPost != null) {
            for (int i = 1; i < commentPost[reading].length; i++) {

                // Check if current position is less than total number of comments for the specified reading
                if (i < notesSize[(reading)]) {
                    try {

                        item = new HashMap<>();
                        String post;
                        String[] poster;

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            //Code for Nougat+

                            //// STRIP UNNECESSARY TEXT ////
                            post = Html.fromHtml(commentPost[reading][i][0].replaceAll("<p style=\"color:#([a-zA-z0-9]+)\">\n" + "\t&nbsp;</p>\n", "").replaceAll("<div>\n" +
                                    "\t&nbsp;</div>", ""), Html.FROM_HTML_MODE_LEGACY).toString().replaceAll("[\\n]{3,}", "\n\n").trim().concat("\n").replaceAll("[\\r]{3,}", "\n\n");
                            poster = Html.fromHtml(commentPost[reading][i][1], Html.FROM_HTML_MODE_LEGACY).toString().replaceAll("Reply to ([a-zA-Z]+)", "").split("Comment added in");


                        } else {
                            //Code for older OS

                            //// REMOVE UNNECESSARY TEXT ////
                            post = Html.fromHtml(commentPost[reading][i][0].replaceAll("<p style=\"color:#([a-zA-z0-9]+)\">\n" + "\t&nbsp;</p>\n", "").replaceAll("<div>\n" +
                                    "\t&nbsp;</div>", "")).toString().replaceAll("[\\n]{3,}", "\n\n").trim().concat("\n").replaceAll("[\\r]{3,}", "\n\n");
                            poster = Html.fromHtml(commentPost[reading][i][1]).toString().replaceAll("Reply to ([a-zA-Z]+)", "").split("Comment added in");

                        }

                        item.put("comment", post);
                        item.put("poster", poster[0].replaceFirst("\\s++$", "") + ", " + poster[1]); // Remove trailing whitespace
                        commentList.add(item);

                    } catch (Exception e) {

                        e.printStackTrace();
                        Log.i("Current iteration", Integer.toString(i));
                        break;

                    }
                }

            }
        }


        readingNotesListView.setAdapter(commentsSA);


        // Re-enable clicking now that comments are loaded!!
        firstReadingTextView.setClickable(true);
        secondReadingTextView.setClickable(true);
        thirdReadingTextView.setClickable(true);


        if (isRefreshing) {

            commentsSwipeRefresh.setRefreshing(false);
            isRefreshing = false;

        }

    } */

    public void toggleViewPagerLoaded() {

        if (commentsDownloaded) {

            commentsViewPager.setVisibility(View.VISIBLE);
            commentsSwipeRefresh.setRefreshing(false);
            commentsSwipeRefresh.setEnabled(false);

        } else {

            commentsViewPager.setVisibility(View.INVISIBLE);
            commentsSwipeRefresh.setRefreshing(true);
            commentsSwipeRefresh.setEnabled(true);


        }

    }


    //Method for selecting comments from title text

    public void updateComments(View view) {

        if (view != null) {

            readingComments = Integer.parseInt(view.getTag().toString());

        }

        Log.i("Change Comments", "Reading " + readingComments + " selected for new notes");


        if (commentsDownloaded) {

            getCommentsProgressBar.setVisibility(View.VISIBLE);

            //updateNotesListView(readingComments);

            //commentsSA.notifyDataSetChanged();
            commentsViewPager.setCurrentItem((readingComments-1));

            Log.i("Comments", "Update complete");

            getCommentsProgressBar.setVisibility(View.INVISIBLE);

        } else {

            Log.i("Comments", "Update failed!");

            commentsConnectErrorText.setVisibility(View.VISIBLE);

        }

    }

    public class DoesFileExist extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... urls) {

            try {
                // We want to check the current URL and not redirected URL
                HttpURLConnection.setFollowRedirects(false);

                HttpURLConnection httpURLConnection = (HttpURLConnection) (new URL(urls[0])).openConnection();

                // We don't need to get data
                httpURLConnection.setRequestMethod("HEAD");
                httpURLConnection.setConnectTimeout(500);
                httpURLConnection.setReadTimeout(15000);

                // Some websites don't like programmatic access so pretend to be a browser
                httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
                Integer responseCode = httpURLConnection.getResponseCode();


                return responseCode;

            } catch (UnknownHostException e) {

                Log.i("Does File Exist", "Connection Error: Unable to resolve host address (No Internet Connection)");
                return 10; //Custom response code signifying no network connection

            } catch (Exception e) {

                e.printStackTrace();
                return 0;
            }

        }

    }

    //////////// Environment Methods ////////////

    //Go to home page on back button pressed
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory( Intent.CATEGORY_HOME );
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
        }

        return super.onKeyDown(keyCode, event);
    }

    // When screen rotates
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        rotateUI();

    }

    //Manage screen rotation
    public void rotateUI() {

        LinearLayout readingsLinearLayout = findViewById(R.id.readingsLinearLayout);
        LinearLayout readingsHeaderLayout = findViewById(R.id.readingsHeaderLayout);
        FrameLayout mainDivider = findViewById(R.id.mainDivider);

        //Set Layout Parameters for main activity
        LinearLayout.LayoutParams horizontalLayout = new LinearLayout.LayoutParams(
                toPixels(240),
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        LinearLayout.LayoutParams verticalLayout = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        //Set divider orientation
        LinearLayout.LayoutParams horizontalBar = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                toPixels(3)
        );

        LinearLayout.LayoutParams verticalBar = new LinearLayout.LayoutParams(
                toPixels(3),
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        //Check screen orientation
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

            readingsLinearLayout.setOrientation(LinearLayout.VERTICAL);
            mainDivider.setLayoutParams(horizontalBar);
            readingsHeaderLayout.setLayoutParams(verticalLayout);

            // Return the date to default

            if (curDay != null && curMonth != null) {

                if (selectedMonth != null && selectedDay != null) {

                    dateTextView.setText(selectedDay.toString() + " " + MONTHS[selectedMonth]);

                } else {

                    dateTextView.setText(curDay.toString() + " " + curMonth);

                }

            }

        } else {

            readingsLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            mainDivider.setLayoutParams(verticalBar);
            readingsHeaderLayout.setLayoutParams(horizontalLayout);

            //Todo: fix portrait title error on theme change

            //Set concise date for landscape view

            if (curDay != null && curMonth != null) {

                dateTextView.setText(selectedDay.toString() + " " + MONTHS[selectedMonth].substring(0,3));

            }

        }

    }

    @Override
    public void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);

        //Check that intent is from activity_readings
        if (newIntent.getStringExtra("fromActivity").equals("readings")) {

            //Update comments to match active reading
            Integer activeReading = newIntent.getIntExtra("activeReading", 0);
            commentsViewPager.setCurrentItem(activeReading);
            Log.i("Comments", "Updating comments to match Reading " + Integer.toString((activeReading)));

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("onResume", "Resuming main activity");

        //Force theme reload if theme has been changed by activity_settings
        if (forceRestart) {
            Utils.restartActivity(this);
            forceRestart = false;
        }

        if (forceUIOrientationCheck) {

            rotateUI();
            forceUIOrientationCheck = false;

        }


    }


    @Override
    public void onPause() {
        super.onPause();
        Log.i("onPause", "Pausing main activity");

    }

    /*
    //Save comments on tab change
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putSerializable("commentPost", commentPost);
        savedInstanceState.putBoolean("commentsDownloaded", commentsDownloaded);

        //ToDo: Save current readings open

        Log.i("State Change", "Saving currents comments state");
        // etc.
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        commentsDownloaded = savedInstanceState.getBoolean("commentsDownloaded");
        commentPost = (String[][][]) savedInstanceState.getSerializable("commentPost");
        Log.i("State Change", "Restoring comments state");

    }
    */


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //go to tab
                    return true;
                case R.id.navigation_readings:
                    //go to tab

                    fragment_viewpager readingsFragment = fragment_viewpager.newInstance("Readings");

                    //Change fragments
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.fragmentContent, readingsFragment).commit();

                    return true;
                case R.id.navigation_comments:
                    //go to tab


                    fragment_viewpager commentsFragment = fragment_viewpager.newInstance("Comments");

                    //Change fragments
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.fragmentContent, commentsFragment).commit();

                    return true;
                case R.id.navigation_map:
                    //go to tab

                    fragment_viewpager mapsFragment = fragment_viewpager.newInstance("Maps");

                    //Change fragments
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.fragmentContent, mapsFragment).commit();

                    return true;
            }
            return false;
        }
    };

}