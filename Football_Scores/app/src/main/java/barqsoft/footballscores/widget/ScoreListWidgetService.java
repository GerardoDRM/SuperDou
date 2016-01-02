package barqsoft.footballscores.widget;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * Created by gerardo on 30/12/15.
 */
public class ScoreListWidgetService extends RemoteViewsService {
    private static final String[] MATCH_COLUMNS = {
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL

    };
    // these indices must match the projection
    private static final int INDEX_DATE = 0;
    private static final int INDEX_HOME = 1;
    private static final int INDEX_AWAY = 2;
    private static final int INDEX_GOALS_HOME = 3;
    private static final int INDEX_GOALS_AWAY = 4;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                // Get matches from current day
                Date fragmentdate = new Date(System.currentTimeMillis());
                SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                String currentDate = mformat.format(fragmentdate);
                Uri firstMatchUri = DatabaseContract.scores_table.buildScoreWithDate();
                data = getContentResolver().query(firstMatchUri, MATCH_COLUMNS, null,
                        new String[]{currentDate}, DatabaseContract.scores_table.DATE_COL + " ASC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.score_list_widget_item);

                // Extract the match data from the Cursor
                String matchHome = data.getString(INDEX_HOME);
                String matchAway = data.getString(INDEX_AWAY);
                String goalsHome = data.getString(INDEX_GOALS_HOME);
                String goalsAway = data.getString(INDEX_GOALS_AWAY);


                // Add the data to the RemoteViews
                views.setImageViewResource(R.id.appwidget_list_home_img, Utilies.getTeamCrestByTeamName(matchHome, getApplicationContext()));
                views.setImageViewResource(R.id.appwidget_list_away_img, Utilies.getTeamCrestByTeamName(matchAway, getApplicationContext()));
                views.setTextViewText(R.id.appwidget_list_score, Utilies.getScores(Integer.parseInt(goalsHome), Integer.parseInt(goalsAway)));
                views.setContentDescription(R.id.appwidget_list_score, getString(R.string.scores, Integer.parseInt(goalsHome), Integer.parseInt(goalsAway)));
                views.setTextViewText(R.id.appwidget_list_home_name, matchHome);
                views.setContentDescription(R.id.appwidget_list_home_name, getString(R.string.team_name, matchHome));
                views.setTextViewText(R.id.appwidget_list_away_name, matchAway);
                views.setContentDescription(R.id.appwidget_list_away_name, getString(R.string.team_name, matchAway));

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.score_list_widget_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }
        };
    }
}
