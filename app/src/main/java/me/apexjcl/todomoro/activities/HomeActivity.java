package me.apexjcl.todomoro.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.SyncUser;
import me.apexjcl.todomoro.R;
import me.apexjcl.todomoro.adapters.HomePagerAdapter;
import me.apexjcl.todomoro.fragments.CreateTaskFragment;
import me.apexjcl.todomoro.fragments.tabs.TasksFragment;
import me.apexjcl.todomoro.realm.UserManager;
import me.apexjcl.todomoro.realm.handlers.PomodoroListHandler;
import me.apexjcl.todomoro.realm.handlers.TaskHandler;
import me.apexjcl.todomoro.realm.models.PomodoroStatus;
import me.apexjcl.todomoro.realm.models.Task;

/**
 * Principal application activity
 * Created by apex on 22/04/17.
 */
public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private boolean logoutAfterClose = false;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @BindView(R.id.navigation_view)
    NavigationView mNavigationView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // Check if a session already exists
        if (savedInstanceState == null) {
            if (UserManager.isSessionAvailable()) {
                UserManager.setActiveUser(SyncUser.currentUser());
            } else {
                logout();
            }
        }
//        Realm realm = Realm.getDefaultInstance();
//        realm.beginTransaction();
//        realm.where(PomodoroStatus.class).findAll().deleteAllFromRealm();
//        realm.commitTransaction();
//        realm.close();
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        setSupportActionBar(mToolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        toggle.getDrawerArrowDrawable().setColor(Color.WHITE);
        mNavigationView.setNavigationItemSelectedListener(this);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();
        loadFragment();
    }

    private void loadFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentHolder, new TasksFragment());
        ft.commit();
    }


    private void logout() {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.setAction(MainActivity.ACTION_IGNORE_USER);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        logoutAfterClose = true;
    }

    @Override
    protected void onStop() {
        if (logoutAfterClose) {
            UserManager.logoutCurrentUser();
            logoutAfterClose = false;
        }
        super.onStop();
    }


    public void showCreateTaskFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentHolder, new CreateTaskFragment());
        ft.addToBackStack(CreateTaskFragment.TAG);
        ft.commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_logout:
                logout();
                break;
            default:
                break;
        }
        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
