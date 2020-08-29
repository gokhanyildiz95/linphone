package org.linphone.task;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import java.util.HashMap;
import org.linphone.DatabaseHelper;
import org.linphone.R;
import org.linphone.activities.MainActivity;

public class TaskActivityView extends MainActivity {
    public static final String NAME = "Task";
    private DatabaseHelper databaseHelper;
    private TaskFragment taskFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        databaseHelper = new DatabaseHelper(this);

        HashMap<String, String> userData = databaseHelper.getUser();
        String jwt = userData.get("jwt");
        String domain_id = userData.get("domain_id");
        getIntent().putExtra("Activity", NAME);
        getIntent().putExtra("jwt", jwt);
        getIntent().putExtra("domain_id", domain_id);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment == null) {
            showTasks();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTaskSelected.setVisibility(View.VISIBLE);
    }

    private void showTasks() {
        Bundle extras = new Bundle();
        extras.putString("jwt", getIntent().getStringExtra("jwt"));
        extras.putString("domain_id", getIntent().getStringExtra("domain_id"));
        taskFragment = new TaskFragment();
        taskFragment.setArguments(extras);
        changeFragment(taskFragment, "Tasks", false);
    }
}
