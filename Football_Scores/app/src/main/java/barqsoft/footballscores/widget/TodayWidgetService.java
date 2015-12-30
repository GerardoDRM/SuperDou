package barqsoft.footballscores.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by gerardo on 29/12/15.
 */
public class TodayWidgetService extends IntentService {

    private static final String[] MATCH_COLUMNS = {
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL

    };
    // these indices must match the projection
    private static final int INDEX_DATE = 0;
    private static final int INDEX_TIME = 1;
    private static final int INDEX_HOME = 2;
    private static final int INDEX_AWAY = 3;
    private static final int INDEX_GOALS_HOME = 4;
    private static final int INDEX_GOALS_AWAY = 5;


    public TodayWidgetService() {
        super("TodayWidgetService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                TodayWidgetMatch.class));


        Date fragmentdate = new Date(System.currentTimeMillis());
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = mformat.format(fragmentdate);

        Uri firstMatchUri = DatabaseContract.scores_table.buildScoreWithDate();
        Cursor data = getContentResolver().query(firstMatchUri, MATCH_COLUMNS, null,
                new String[]{currentDate}, DatabaseContract.scores_table.DATE_COL + " ASC");

        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        // Extract the match data from the Cursor
        String matchTime = data.getString(INDEX_TIME);
        String matchHome = data.getString(INDEX_HOME);
        String matchAway = data.getString(INDEX_AWAY);
        String goalsHome = data.getString(INDEX_GOALS_HOME);
        String goalsAway = data.getString(INDEX_GOALS_AWAY);

        data.close();

        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.today_widget_match);

            // Add the data to the RemoteViews
            views.setImageViewResource(R.id.appwidget_home_img, Utilies.getTeamCrestByTeamName(matchHome));
            views.setImageViewResource(R.id.appwidget_away_img, Utilies.getTeamCrestByTeamName(matchAway));
            views.setTextViewText(R.id.appwidget_time, matchTime);
            views.setTextViewText(R.id.appwidget_score, getString(R.string.widget_score,
                    Utilies.getScores(Integer.parseInt(goalsHome), Integer.parseInt(goalsAway))));
            views.setContentDescription(R.id.appwidget_score, getString(R.string.scores,Integer.parseInt(goalsHome), Integer.parseInt(goalsAway)));
            views.setTextViewText(R.id.appwidget_home_name, matchHome);
            views.setContentDescription(R.id.appwidget_home_name, getString(R.string.team_name, matchHome));
            views.setTextViewText(R.id.appwidget_away_name, matchAway);
            views.setContentDescription(R.id.appwidget_away_name, getString(R.string.team_name,matchAway));


            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }
}
