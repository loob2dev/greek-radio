package com.Greek.Radios.activities;

import com.Greek.Radios.R;
import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.TitlePage;
import com.stephentuso.welcome.WelcomeConfiguration;

/**
 * Created by aykut on 14.01.2018.
 */

public class WelcomeActivity extends com.stephentuso.welcome.WelcomeActivity {

    protected WelcomeConfiguration configuration() {
        return new WelcomeConfiguration.Builder(this)
                .defaultBackgroundColor(R.color.colorPrimary2)
                .page(new BasicPage(R.drawable.screen1,
                        getString(R.string.app_screen_hello),
                        getString(R.string.app_screen1))
                )
                .page(new BasicPage(R.drawable.screen2,
                        getString(R.string.app_screen2_t),
                        getString(R.string.app_screen2))
                )
                .page(new BasicPage(R.drawable.screen3,
                        getString(R.string.app_screen3_t),
                        getString(R.string.app_screen3))
                )
                .swipeToDismiss(true)
                .build();
    }


}
