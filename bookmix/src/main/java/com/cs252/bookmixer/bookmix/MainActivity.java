package com.cs252.bookmixer.bookmix;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    ListView listView;
    ArrayAdapter<Book> bookAdapter;
    ArrayAdapter<String> stringAdapter; // strings for the listView
    ArrayList<Book> selectedItems;
    //ArrayList<String> selectedItems;

    TextView resultTextView;
    Button generateButton;
    ProgressDialog mProgressDialog;

    DatabaseAdapter db;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init the db
        db = new DatabaseAdapter(this);
        //db.resetDB();
        db.createDatabase();
        db.open();

        /*
        try {
            System.out.println("adding test book");
            db.addBook(new Book(0, "Metamorphosis", "Franz Kafka", "http://www.gutenberg.org/cache/epub/5200/pg5200.txt"));
            db.addBook(new Book(1, "The Adventures of Tom Sawyer", "Mark Twain", "http://www.gutenberg.org/cache/epub/74/pg74.txt"));
            db.addBook(new Book(2, "The Importance of Being Earnest", "Oscar Wilde", "http://www.gutenberg.org/cache/epub/844/pg844.txt"));
        } catch (SQLiteConstraintException e) {
            Log.e("MainActivity", "books were already added!");
        } catch (SQLiteException e) {
            Log.e("MainActivity", "no books table");
        }
        */

        // instantiate progressBar
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("A message");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);


        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Log.d(TAG, "selected: " + tab.getText());

        if (tab.getText().equals(getString(R.string.title_section2))) {

            // update the textview to reflect what's selected
            Log.d(TAG, "Updating result textview");

            StringBuilder resultText = new StringBuilder();
            resultText.append("Selected Items: \n\n");
            for (Book b : selectedItems) {
                resultText.append(b.toString());
                resultText.append("\n");
            }

            resultTextView.setText(resultText.toString());
        }

        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Log.d(TAG, "unselected: " + tab.getText());

        // sloppily match text to check what tab we have
        if (tab.getText().equals(getString(R.string.title_section1))) {
            SparseBooleanArray checked = listView.getCheckedItemPositions();
            selectedItems = new ArrayList<Book>();

            for (int i = 0; i < checked.size(); i++) {
                int position = checked.keyAt(i);
                if (checked.valueAt(i)) {
                    Log.d(TAG, "Selected item: " + bookAdapter.getItem(position).toString());
                    selectedItems.add(bookAdapter.getItem(position));
                }
            }
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0) {
                return new BookSelectFragment();
            } else if (position == 1) {
                return new MashupFragment();
            } else {  // returns the default if needed for some reason (rather than just breaking)
                return PlaceholderFragment.newInstance(position + 1);
            }
        }

        @Override
        public int getCount() {
            // number of total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1);
                case 1:
                    return getString(R.string.title_section2);
            }
            return null;
        }
    }

    // listview to select books
    public class BookSelectFragment extends Fragment {

        private void setListViewHandler(View view) {
            listView = (ListView) view.findViewById(R.id.bookList);

            // convert list of book objects into string[]
            List<Book> list = db.getAllBooks();
            Book[] books = new Book[list.size()];
            int i = 0;
            for (Book b : list) {
                books[i] = b;
                i++;
            }

            bookAdapter = new ArrayAdapter<Book>(super.getActivity(),
                    android.R.layout.simple_list_item_multiple_choice, books);
            //stringAdapter = new ArrayAdapter<String>(super.getActivity(),
            //        android.R.layout.simple_list_item_multiple_choice, books);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            listView.setAdapter(bookAdapter);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d(TAG, "Creating book select view");
            View rootView = inflater.inflate(R.layout.fragment_bookselect, container, false);
            setListViewHandler(rootView);
            return rootView;
        }
    }

    protected class DownloadTask extends AsyncTask<Book, Integer, Book> {
        private Context context;
        private PowerManager.WakeLock mWakeLock;
        String output;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected Book doInBackground(Book ... books) {
            output = null;
            InputStream input = null;
            HttpURLConnection connection = null;
            try {
                Log.d(TAG, "attempting dl from url: " + books[0].getURL());
                URL url = new URL(books[0].getURL());
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage());
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                List values = connection.getHeaderFields().get("content-Length");
                int fileLength = 0;
                if (values != null && !values.isEmpty()) {
                    String sLength = (String) values.get(0);
                    if (sLength != null) {
                        fileLength = Integer.parseInt(sLength);
                    }
                }

                Log.d(TAG, "file length: " + fileLength);

                // download the file
                input = connection.getInputStream();

                BufferedReader br = null;
                StringBuilder sb = new StringBuilder();

                String line;
                long total = 0;
                int count;
                br = new BufferedReader(new InputStreamReader(input));
                while ((line = br.readLine()) != null) {
                    // publishing the progress....
                    total += 1;
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    Log.d(TAG, line);

                    sb.append(line);
                }

                Log.d(TAG, "writing to output string");
                output = sb.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (output != null)
                        //output.close();
                    if (input != null)
                        input.close();
                } catch (IOException i) {
                    i.printStackTrace();
                }

                if (connection != null) {
                    connection.disconnect();
                }
            }
            Log.d(TAG, "returning from downloader");

            books[0]._text = output.toString();
            return books[0];
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        protected void onPostExecute(Book result) {
            Log.d(TAG, "doing postExecute");
            mWakeLock.release();
            mProgressDialog.dismiss();
            Toast toast = new Toast(getApplicationContext());
            /*
            if (result != null) {
                toast.makeText(context,"Download error: "+result, Toast.LENGTH_LONG).show();
            } else {
                resultTextView.setText(output.toString());
                toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();
            }
            */

            resultTextView.setText(result._text);
        }
    }

    // interface for actually generating the mashups
    public class MashupFragment extends Fragment {

        // handler for the generate button
        private void setGenerateButtonHandler(View view) {
            generateButton = (Button) view.findViewById(R.id.generate_button);
            generateButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "Mashing up selected books");
                    // download books if necessary

                    for (Book book : selectedItems) {
                        Log.d(TAG, "Selected: "+ book.toString() + " isDownloaded? " + book.is_downloaded());
                        Log.d(TAG, "first text: " + book.get_text().toString().split(" "));
                        if (!book.is_downloaded()) {
                            // then download it!
                            Log.d(TAG, "Must DL: "+ book.toString());
                            final DownloadTask dt = new DownloadTask(getApplicationContext());
                            dt.execute(book);

                            Log.d(TAG, "post download");
                        }
                    }
                }
            });
        }

        private void setTextViewAdapter(View view) {
            resultTextView = (TextView) view.findViewById(R.id.result_text);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d(TAG, "Creating mashup view");
            View rootView = inflater.inflate(R.layout.fragment_mashup, container, false);

            setTextViewAdapter(rootView);
            setGenerateButtonHandler(rootView);

            return rootView;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    // convert InputStream to String
    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                Log.d(TAG, line);
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    Log.d(TAG, "closing buffered reader");
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

}
