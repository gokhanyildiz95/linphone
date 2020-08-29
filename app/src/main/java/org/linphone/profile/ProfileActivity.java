package org.linphone.profile;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import org.linphone.DatabaseHelper;
import org.linphone.R;
import org.linphone.activities.MainActivity;

public class ProfileActivity extends MainActivity {
    private TextView mName, mUsername, mNumber, mEmail, mExtension, mTenantId;
    private ImageView mAvatar;
    protected ImageView mBack;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        databaseHelper = new DatabaseHelper(this);
        mOnBackPressGoHome = false;
        mAlwaysHideTabBar = true;
        mAvatar = findViewById(R.id.profile_layout_avatar);
        mName = findViewById(R.id.profile_layout_name_top);
        mUsername = findViewById(R.id.profile_layout_name);
        mEmail = findViewById(R.id.profile_layout_email);
        // mNumber = findViewById(R.id.profile_layout_number);
        mExtension = findViewById(R.id.profile_layout_extension);
        mTenantId = findViewById(R.id.profile_layout_tenant_id);

        mBack = findViewById(R.id.profile_back);
        mBack.setOnClickListener(v -> finish());
        mName.setText(databaseHelper.getFullname());
        mUsername.setText(databaseHelper.getUsername());
        mEmail.setText(databaseHelper.getUserEmail());
        mExtension.setText(databaseHelper.getExtension());
        mTenantId.setText(databaseHelper.getTenantId());

        final String avatar_url = databaseHelper.getUserAvatar();

        Glide.with(this)
                .load(databaseHelper.getUserAvatar())
                .apply(RequestOptions.circleCropTransform())
                .into(mAvatar);
    }

    @Override
    protected void onDestroy() {
        mBack = null;
        mName = null;
        mEmail = null;
        mExtension = null;
        mTenantId = null;

        super.onDestroy();
    }
}
