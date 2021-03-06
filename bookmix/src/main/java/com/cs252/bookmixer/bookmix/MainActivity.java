package com.cs252.bookmixer.bookmix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    DatabaseAdapter db;

    BookAdapter bookAdapter;
    ArrayList<Book> selectedItems;
    ListView listView;
    TextView outputTextView;
    Button generateButton;
    ProgressDialog progressDialog;
    MarkovGen markovGen;

    private static final String TAG = "MainActivity"; // for debugging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init the db
        db = new DatabaseAdapter(this);
        db.resetDB();  // uncomment for db debugging
        db.createDatabase();  // copies if necessary, does nothing otherwise
        db.open();

        // instantiate progressBar
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("A message");
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);

        selectedItems = new ArrayList<Book>();

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
        } else if (id == R.id.action_dbreset) {
            Toast.makeText(getApplicationContext(),
                    "Resetting the database...", Toast.LENGTH_LONG).show();
            db.close();
            db.resetDB();
            db.createDatabase();  // copies if necessary, does nothing otherwise
            db.open();
            Toast.makeText(getApplicationContext(),
                    "Finished.", Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Log.d(TAG, "selected: " + tab.getText());

        if (tab.getText().equals(getString(R.string.title_section2))) {

            selectedItems = bookAdapter.getSelectedItems();

            // update the textview to reflect what's selected
            StringBuilder resultText = new StringBuilder();
            resultText.append("Selected Items: \n\n");
            for (Book b : selectedItems) {
                resultText.append(b.toString());
                resultText.append("\n");
            }

            outputTextView.setText(resultText.toString());
        }

        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Log.d(TAG, "unselected: " + tab.getText());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            if (position == 0) {
                return new BookSelectFragment();
            } else if (position == 1) {
                return new MashupFragment();
            } else {
                return null;
            }
        }

        @Override
        public int getCount() { // number of total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_section1);
                case 1:
                    return getString(R.string.title_section2);
            }
            return null; // ya dun goofed
        }
    }

    // fragment with listview to select books
    public class BookSelectFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d(TAG, "Creating book select fragment");
            View rootView = inflater.inflate(R.layout.fragment_bookselect, container, false);
            setListViewHandler(rootView);
            return rootView;
        }

        private void setListViewHandler(View view) {
            listView = (ListView) view.findViewById(R.id.bookList);

            // convert list of books into array[]
            List<Book> list = db.getAllBooks();

            // set adapter
            bookAdapter = new BookAdapter(super.getActivity(), R.layout.listcell, list);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); // able to select multiples
            listView.setAdapter(bookAdapter);
        }
    }

    private class BookAdapter extends ArrayAdapter<Book> {
        private List<Book> books;
        public ArrayList<Boolean> checkedItems;
        Context context;


        public BookAdapter(Context context, int textViewResourceId, List<Book> books) {
            super(context, textViewResourceId, books);
            this.books = books;
            this.context = context;

            //initialize to all zeros
            checkedItems = new ArrayList<Boolean>(Collections.nCopies(books.size(), false));
        }

        public ArrayList<Book> getSelectedItems() {
            ArrayList<Book> selectedItems = new ArrayList<Book>();
            for (int i = 0; i < books.size(); i++) {
                if (checkedItems.get(i)) { // is selected
                    selectedItems.add(books.get(i));
                }
            }
            return selectedItems;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) { // create a new one
                LayoutInflater inflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listcell, null);

                holder = new ViewHolder();
                holder.checkBox = (CheckBox) convertView.findViewById( R.id.checkbox );
                holder.title = (TextView) convertView.findViewById(R.id.title_text);
                holder.author = (TextView) convertView.findViewById(R.id.author_text);
                holder.cellView = convertView.findViewById(R.layout.listcell);

                holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        int position = (Integer) compoundButton.getTag();
                        checkedItems.set(position, compoundButton.isChecked());
                    }
                });

                convertView.setTag(holder);
                convertView.setTag(R.id.title_text, holder.title);
                convertView.setTag(R.id.author_text, holder.author);
                convertView.setTag(R.id.checkbox, holder.checkBox);
                convertView.setTag(R.layout.listcell, holder.cellView);

            } else { // recycle an old one
                holder = (ViewHolder) convertView.getTag();
            }

            holder.checkBox.setTag(position);

            Book b = books.get(position);

            holder.title.setText(b.get_title());
            holder.author.setText(b.get_author());
            holder.checkBox.setChecked( checkedItems.get( position ) );

            return convertView;
        }

        @Override
        public int getCount() {
            return books.size();
        }

        @Override
        public Book getItem(int position) {
            return books.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    }

    static class ViewHolder {
        TextView title;
        TextView author;
        CheckBox checkBox;
        View cellView;
    }

    private class DownloadTextTask extends AsyncTask<Book, Integer, Book> {
        static final String TAG = "DownloadTextTask: ";
        //Toast toast = new Toast(getApplicationContext());
        boolean inChargeOfProgress = false;

        Book originalBook;

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTextTask(Context context) {
            this.context = context;
        }

        @Override
        protected Book doInBackground(Book ... books) {
            originalBook = books[0];

            InputStream input = null;
            HttpURLConnection connection = null;
            String downloadedText;
            Book bookWithText;

            try {
                Log.d(TAG, "attempting dl from url: " + books[0].getURL());
                URL url = new URL(books[0].getURL());
                connection = (HttpURLConnection) url.openConnection();
                populateDesktopHttpHeaders(connection);
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage());
                }

                // actually download the file
                input = connection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(input));
                StringBuilder sb = new StringBuilder();

                String line;
                long total = 0;
                long fileSize = books[0].getFilesize();
                System.out.print("writing to buffered reader");

                while (true) {
                    line = br.readLine();
                    if (line == null) {
                        break;
                    }

                    // publish progress...
                    total += line.length();
                    if (fileSize > 0) {// only if total length is known
                        publishProgress((int) (total * 100 / fileSize));
                    }
                    sb.append(line+" ");
                }
                br.close(); // done with buffered reader

                // put the text in the book
                Log.d(TAG, "writing to output string");
                downloadedText = sb.toString();
                bookWithText = books[0];
                int startPos=downloadedText.indexOf("START OF THIS PROJECT GUTENBERG");
                if (startPos==-1) startPos=downloadedText.indexOf("START OF THE PROJECT GUTENBERG");
                int endPos = downloadedText.indexOf("END OF THIS PROJECT GUTENBERG");
                if (endPos==-1) endPos=downloadedText.indexOf("START OF THE PROJECT GUTENBERG");
                if(startPos!=-1 && endPos!=-1){
                    bookWithText.set_text(downloadedText.substring(startPos,endPos));
                } else{
                    System.out.println("Warning! Failed to trim. Start: "+startPos+" End: "+endPos);
                    bookWithText.set_text(downloadedText);
                }
                markovGen.addDatum(downloadedText.substring(startPos,endPos));

            } catch (Exception e) {
                e.printStackTrace();
                return null; // :(
            } finally { // clean up the connection
                Log.d(TAG, "executing 'finally'");
                try {
                    if (input != null) {
                        Log.d(TAG, "closing input");
                        input.close();
                    }
                } catch (IOException i) {
                    i.printStackTrace();
                }
                if (connection != null) {
                    Log.d(TAG, "disconnecting connection");
                    connection.disconnect();
                }
            }

            Log.d(TAG, "updating DB with new book");
            db.updateBook(bookWithText);

            Log.d(TAG, "returning from downloader");
            return bookWithText;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(context,"Downloading", Toast.LENGTH_LONG).show();

            //Log.d(TAG, "Showing progress bar");
            if (!progressDialog.isShowing()) {
                inChargeOfProgress = true;
                progressDialog.show();
                progressDialog.setMessage("Downloading");
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            if (!progressDialog.isShowing()) {
                Log.d(TAG, "taking control of progress");
                inChargeOfProgress = true;
                progressDialog.show();
                progressDialog.setMessage("Downloading " + originalBook.get_title());
            }

            if (inChargeOfProgress) {
                progressDialog.setIndeterminate(false);
                progressDialog.setMax(100);
                progressDialog.setProgress(progress[0]);
            }
        }

        @Override
        protected void onPostExecute(Book result) {
            Log.d(TAG, "doing postExecute");

            //mWakeLock.release();
            if (inChargeOfProgress) {
                progressDialog.dismiss();
                inChargeOfProgress = false;
            }

            if (result == null) {
                // null because AsyncTask hasn't done its task
                Toast.makeText(context,"Download error", Toast.LENGTH_LONG).show();

            } else {
                Log.v(TAG, "File downloaded");
                outputTextView.setText(result.toString());
                Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // fragment for generating the mashups
    public class MashupFragment extends Fragment {

        private void setMashupButtonHanlder(View view) {
            Button mashup_button = (Button) view.findViewById(R.id.mashup_button);
            mashup_button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    StringBuilder markovString = new StringBuilder();
                    for (String s : markovGen.nextNSentences(10)) {
                        markovString.append(s);
                        markovString.append(" ");
                    }
                    outputTextView.setText(markovString.toString());
                }
            });
        }

        // handler for the generate button
        // we download text from gutenberg here
        private void setGenerateButtonHandler(View view) {
            generateButton = (Button) view.findViewById(R.id.generate_button);
            generateButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "Mashing up selected books");

                    markovGen = new MarkovGen();

                    // first, download books if necessary
                    for (Book book : selectedItems) {
                        Log.d(TAG, "Selected: " + book.toString()
                                + " isDownloaded? " + book.is_downloaded());

                        if (!book.is_downloaded()) { // then download it!
                            Log.d(TAG, "Downloading: " + book.toString());

                            Context context = getApplicationContext();

                            // multi-thread if possible
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                Log.d(TAG, "such multithreading wow");
                                new DownloadTextTask(context)
                                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, book);
                            } else {
                                try {
                                    new DownloadTextTask(context).execute(book).get();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } else { // book is downloaded
                            Toast.makeText(getApplicationContext(),"Generating Markov Chains",
                                    Toast.LENGTH_LONG).show();
                            markovGen.addDatum(book.get_text());
                        }
                    }
                }
            });
        }

        private void setTextViewAdapter(View view) {
            outputTextView = (TextView) view.findViewById(R.id.result_text);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.d(TAG, "Creating mashup fragment");
            View rootView = inflater.inflate(R.layout.fragment_mashup, container, false);

            markovGen = new MarkovGen();

            setTextViewAdapter(rootView);
            setGenerateButtonHandler(rootView);
            setMashupButtonHanlder(rootView);

            return rootView;
        }
    }

    private static void populateDesktopHttpHeaders(URLConnection urlCon) {
        // add custom header in order to be easily detected
        urlCon.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
        urlCon.setRequestProperty("Accept-Language",
                "el-gr,el;q=0.8,en-us;q=0.5,en;q=0.3");
        urlCon.setRequestProperty("Accept-Charset",
                "ISO-8859-7,utf-8;q=0.7,*;q=0.7");
    }
}
