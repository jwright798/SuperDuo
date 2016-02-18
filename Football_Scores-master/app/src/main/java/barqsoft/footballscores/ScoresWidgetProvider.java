package barqsoft.footballscores;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jeremywright on 2/16/16.
 * This widget will show the first game of the day, clicking on the game will launch into MainActivity
 */
public class ScoresWidgetProvider extends AppWidgetProvider {

    //getting constants from the scoresAdapter
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // update each of the app widgets with the remote adapter
        for (int widgetId : appWidgetIds) {
            //Set up the View
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.scores_widget_layout);

            //get today's date in the right format(from myFetchService)
            Date today = new Date(System.currentTimeMillis());
            SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
            String[] dateArray = new String[1];
            dateArray[0] = mformat.format(today);

            //get the list of games from the DB
            CursorLoader cursorLoader = new CursorLoader(context, DatabaseContract.scores_table.buildScoreWithDate(),
                    null, null, dateArray, null);

            //use the loadInBackground to get the data, since we can't use onLoadFinished
            Cursor gamesInfo = cursorLoader.loadInBackground();
            if (gamesInfo != null && gamesInfo.getCount()>0){
                //get the first game
                gamesInfo.moveToFirst();
                Log.v("Widget", "Home:" + gamesInfo.getString(COL_HOME));
                Log.v("Widget","Away:"+gamesInfo.getString(COL_AWAY));
                Log.v("Widget","Score:"+Utilies.getScores(gamesInfo.getInt(COL_HOME_GOALS), gamesInfo.getInt(COL_AWAY_GOALS)));
                views.setTextViewText(R.id.game_1_home_team,gamesInfo.getString(COL_HOME));
                views.setTextViewText(R.id.game_1_away_team,gamesInfo.getString(COL_AWAY));
                views.setTextViewText(R.id.game_1_score, Utilies.getScores(gamesInfo.getInt(COL_HOME_GOALS), gamesInfo.getInt(COL_AWAY_GOALS)));
            }
            else {
                //no games found, so display message to user
                //no access to text view visibility to just hide the views, so setting them to empty
                views.setTextViewText(R.id.game_1_away_team, "");
                views.setTextViewText(R.id.game_1_home_team, "");
                views.setTextViewText(R.id.game_1_score, "No games today!");
            }
            gamesInfo.close();

            //add an intent to deepdive into MainActivity
            //http://www.androidauthority.com/create-simple-android-widget-608975/
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.scores_layout, pendingIntent);

            appWidgetManager.updateAppWidget(widgetId, views);
        }

    }
}
