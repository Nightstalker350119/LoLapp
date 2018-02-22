package com.norbertotaveras.game_companion_app.ChampionPage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ListView;

import com.norbertotaveras.game_companion_app.R;

import java.util.ArrayList;

/**
 * Created by Emanuel on 12/7/2017.
 */

public class ChampionsWinRate extends Fragment {

    private static final String TAG = "ChampionsWinRate";
    private static final String BASE_URL = "api.champion.gg/v2";

    private ListView listView;
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> mWinRates = new ArrayList<>(); //Needs api calls, use placeholders atm
    private ArrayList<String> mChampionPosition = new ArrayList<>(); //

    float alpha = 1.0f;
    private int ScrollAmount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_championswinrate, container, false);

        Log.d(TAG, "onCreate: starting.");

        final Button btnTop = rootView.findViewById(R.id.topButton);
        final RecyclerView rView = rootView.findViewById(R.id.winrecyclerview);
//        btnTop.setEnabled(false);

        rView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = ((LinearLayoutManager) rView.getLayoutManager());
                int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();

                if (firstVisiblePosition > 1) // && ScrollAmount < dy // Scroll has to be less than current y in order for us to realize it's scrolling up
                {
                    btnTop.setEnabled(true);
                    buttonAnimation(btnTop);
                    //ScrollAmount = dy;
                    btnTop.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            rView.getLayoutManager().scrollToPosition(0);
                            btnTop.setEnabled(false);
                            btnTop.setAlpha(0);
                            Log.d(TAG, "ButtonState Disabler: onClick");
                        }
                    });

                } else {
                    btnTop.setVisibility(View.INVISIBLE);
                    btnTop.setEnabled(false);
                    Log.d(TAG, "ButtonState Disabler: else");
                }

            }
        });


        //initImageBitmaps();



        return rootView;
    }


    public void buttonAnimation(final Button button) { // Timing and animation effects
        Animation btn = new AlphaAnimation(1.00f, 0.00f);
        btn.setDuration(3000);
        btn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                button.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                button.setVisibility(View.INVISIBLE);
                button.setEnabled(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        button.startAnimation(btn);
    }

    private void initImageBitmaps(){
        Log.d(TAG, "initImageBitmaps: preparing bitmaps.");

        //Call<List<ChampionDTO>> champions = apiService.getChampions();
        //Log.d(TAG, champions.toString());
        //Hardcode names, pictures, and positions but leave winrates to be dynamically allocated to champs.

        mImageUrls.add("http://media.comicbook.com/2017/07/aatrox-0-1005633.jpg");
        mNames.add("Aatrox");
        mWinRates.add("54.76%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/ahri.png");
        mNames.add("Ahri");
        mWinRates.add("54.76%");
        mChampionPosition.add("Middle");

        mImageUrls.add("https://www.mobafire.com/images/avatars/akali-classic.png");
        mNames.add("Akali");
        mWinRates.add("54.76%");
        mChampionPosition.add("Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/rectangle/alistar.png");
        mNames.add("Alistar");
        mWinRates.add("54.76%");
        mChampionPosition.add("Support");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/amumu.png");
        mNames.add("Amumu");
        mWinRates.add("54.76%");
        mChampionPosition.add("Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/anivia.png");
        mNames.add("Anivia");
        mWinRates.add("54.76%");
        mChampionPosition.add("Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/annie.png");
        mNames.add("Annie");
        mWinRates.add("54.76%");
        mChampionPosition.add("Middle | Support");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/ashe.png");
        mNames.add("Ashe");
        mWinRates.add("54.76%");
        mChampionPosition.add("Middle");

        mImageUrls.add("https://www.mobafire.com/images/avatars/aurelion-sol-classic.png");
        mNames.add("Aurelion Sol");
        mWinRates.add("54.76%");
        mChampionPosition.add("Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/square/azir.png");
        mNames.add("Azir");
        mWinRates.add("54.76%");
        mChampionPosition.add("Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/bard.png");
        mNames.add("Bard");
        mWinRates.add("54.76%");
        mChampionPosition.add("Support");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/blitzcrank.png");
        mNames.add("Blitzcrank");
        mWinRates.add("54.76%");
        mChampionPosition.add("Support");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/brand.png");
        mNames.add("Brand");
        mWinRates.add("54.76%");
        mChampionPosition.add("Middle | Support");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/braum.png");
        mNames.add("Braum");
        mWinRates.add("54.76%");
        mChampionPosition.add("Support");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/caitlyn.png");
        mNames.add("Caitlyn");
        mWinRates.add("54.76%");
        mChampionPosition.add("ADC");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/camille.png");
        mNames.add("Camille");
        mWinRates.add("54.76%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/cassiopeia.png");
        mNames.add("Cassiopeia");
        mWinRates.add("54.76%");
        mChampionPosition.add("Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/chogath.png");
        mNames.add("Cho'Gath");
        mWinRates.add("54.76%");
        mChampionPosition.add("Middle | Top | Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/corki.png");
        mNames.add("Corki");
        mWinRates.add("54.76%");
        mChampionPosition.add("ADC | Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/darius.png");
        mNames.add("Darius");
        mWinRates.add("54.76%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/diana.png");
        mNames.add("Diana");
        mWinRates.add("54.76%");
        mChampionPosition.add("Middle | Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/dr-mundo.png");
        mNames.add("Dr. Mundo");
        mWinRates.add("54.76%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/draven.png");
        mNames.add("Draven");
        mWinRates.add("54.76%");
        mChampionPosition.add("ADC");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/ekko.png");
        mNames.add("Ekko");
        mWinRates.add("54.76%");
        mChampionPosition.add("Middle | Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/elise.png");
        mNames.add("Elise");
        mWinRates.add("54.76%");
        mChampionPosition.add("Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/evelynn.png");
        mNames.add("Evelynn");
        mWinRates.add("54.76%");
        mChampionPosition.add("Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/ezreal.png");
        mNames.add("Ezreal");
        mWinRates.add("54.76%");
        mChampionPosition.add("ADC");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/fiddlesticks.png");
        mNames.add("Fiddlesticks");
        mWinRates.add("54.76%");
        mChampionPosition.add("Middle | Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/square/fiora.png");
        mNames.add("Fiora");
        mWinRates.add("54.76%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/fizz.png");
        mNames.add("Fizz");
        mWinRates.add("54.76%");
        mChampionPosition.add("Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/galio.png");
        mNames.add("Galio");
        mWinRates.add("54.76%");
        mChampionPosition.add("Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/gangplank.png");
        mNames.add("Gangplank");
        mWinRates.add("53.13%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/garen.png");
        mNames.add("Garen");
        mWinRates.add("53.13%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/gnar.png");
        mNames.add("Gnar");
        mWinRates.add("53.13%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/gragas.png");
        mNames.add("Gragas");
        mWinRates.add("53.13%");
        mChampionPosition.add("Jungle | Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/graves.png");
        mNames.add("Graves");
        mWinRates.add("53.13%");
        mChampionPosition.add("ADC");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/hecarim.png");
        mNames.add("Hecarim");
        mWinRates.add("53.13%");
        mChampionPosition.add("Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/heimerdinger.png");
        mNames.add("Heimerdinger");
        mWinRates.add("53.13%");
        mChampionPosition.add("Top | Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/illaoi.png");
        mNames.add("Illaoi");
        mWinRates.add("53.13%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/irelia.png");
        mNames.add("Irelia");
        mWinRates.add("53.13%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/ivern.png");
        mNames.add("Ivern");
        mWinRates.add("53.13%");
        mChampionPosition.add("Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/janna.png");
        mNames.add("Janna");
        mWinRates.add("53.13%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/jarvan-iv.png");
        mNames.add("Jarvan IV");
        mWinRates.add("53.13%");
        mChampionPosition.add("Top");

        mImageUrls.add("http://www.behindthevoiceactors.com/_img/chars/jax-league-of-legends-4.27.jpg");
        mNames.add("Jax");
        mWinRates.add("50.89%");
        mChampionPosition.add("Top | Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/jayce.png");
        mNames.add("Jayce");
        mWinRates.add("53.13%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/jhin.png");
        mNames.add("Jhin");
        mWinRates.add("53.13%");
        mChampionPosition.add("ADC");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/jinx.png");
        mNames.add("Jinx");
        mWinRates.add("53.13%");
        mChampionPosition.add("ADC");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/kalista.png");
        mNames.add("Kalista");
        mWinRates.add("53.13%");
        mChampionPosition.add("ADC");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/karma.png");
        mNames.add("Karma");
        mWinRates.add("53.13%");
        mChampionPosition.add("Support | Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/karthus.png");
        mNames.add("Karthus");
        mWinRates.add("52.94%");
        mChampionPosition.add("Mid");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/kassadin.png");
        mNames.add("Kassadin");
        mWinRates.add("52.94%");
        mChampionPosition.add("Mid");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/katarina.png");
        mNames.add("Katarina");
        mWinRates.add("52.94%");
        mChampionPosition.add("Mid");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/kayle.png");
        mNames.add("Kayle");
        mWinRates.add("52.94%");
        mChampionPosition.add("Mid");

        mImageUrls.add("https://www.mobafire.com/images/avatars/kayn-classic.png");
        mNames.add("Kayn");
        mWinRates.add("49.73%");
        mChampionPosition.add("Jungle | Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/kennen.png");
        mNames.add("Kennen");
        mWinRates.add("52.94%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/khazix.png");
        mNames.add("Kha'Zix");
        mWinRates.add("52.94%");
        mChampionPosition.add("Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/kindred.png");
        mNames.add("Kindred");
        mWinRates.add("52.94%");
        mChampionPosition.add("Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/kled.png");
        mNames.add("Kled");
        mWinRates.add("52.94%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/kogmaw.png");
        mNames.add("Kog'Maw");
        mWinRates.add("52.94%");
        mChampionPosition.add("ADC");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/leblanc.png");
        mNames.add("LeBlanc");
        mWinRates.add("52.94%");
        mChampionPosition.add("Mid");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/lee-sin.png");
        mNames.add("Lee Sin");
        mWinRates.add("52.94%");
        mChampionPosition.add("Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/leona.png");
        mNames.add("Leona");
        mWinRates.add("52.94%");
        mChampionPosition.add("Support");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/lissandra.png");
        mNames.add("Lissandra");
        mWinRates.add("50.44%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/lucian.png");
        mNames.add("Lucian");
        mWinRates.add("50.44%");
        mChampionPosition.add("ADC");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/lulu.png");
        mNames.add("Lulu");
        mWinRates.add("50.44%");
        mChampionPosition.add("Support");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/lux.png");
        mNames.add("Lux");
        mWinRates.add("50.44%");
        mChampionPosition.add("Mid | Support");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/malphite.png");
        mNames.add("Malphite");
        mWinRates.add("50.44%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/malzahar.png");
        mNames.add("Malzahar");
        mWinRates.add("50.44%");
        mChampionPosition.add("Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/maokai.png");
        mNames.add("Maokai");
        mWinRates.add("50.44%");
        mChampionPosition.add("Top | Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/master-yi.png");
        mNames.add("Master Yi");
        mWinRates.add("50.44%");
        mChampionPosition.add("Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/miss-fortune.png");
        mNames.add("Miss Fortune");
        mWinRates.add("40.72%");
        mChampionPosition.add("ADC | Sup");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/mordekaiser.png");
        mNames.add("Mordekaiser");
        mWinRates.add("50.44%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/morgana.png");
        mNames.add("Morgana");
        mWinRates.add("50.44%");
        mChampionPosition.add("Support | Mid");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/nami.png");
        mNames.add("Nami");
        mWinRates.add("50.44%");
        mChampionPosition.add("Support");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/nasus.png");
        mNames.add("Nasus");
        mWinRates.add("50.44%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/nautilus.png");
        mNames.add("Nautilus");
        mWinRates.add("50.44%");
        mChampionPosition.add("Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/nidalee.png");
        mNames.add("Nidalee");
        mWinRates.add("50.44%");
        mChampionPosition.add("Jungle | Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/nocturne.png");
        mNames.add("Nocturne");
        mWinRates.add("50.44%");
        mChampionPosition.add("Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/nunu.png");
        mNames.add("Nunu");
        mWinRates.add("50.44%");
        mChampionPosition.add("Jungle | Support");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/olaf.png");
        mNames.add("Olaf");
        mWinRates.add("50.44%");
        mChampionPosition.add("Top | Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/orianna.png");
        mNames.add("Orianna");
        mWinRates.add("50.44%");
        mChampionPosition.add("Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/ornn.png");
        mNames.add("Ornn");
        mWinRates.add("50.44%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/square/pantheon.png");
        mNames.add("Pantheon");
        mWinRates.add("50.44%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/poppy.png");
        mNames.add("Poppy");
        mWinRates.add("50.44%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/quinn.png");
        mNames.add("Quinn");
        mWinRates.add("50.44%");
        mChampionPosition.add("Top | ADC");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/rakan.png");
        mNames.add("Rakan");
        mWinRates.add("50.44%");
        mChampionPosition.add("Support");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/rammus.png");
        mNames.add("Rammus");
        mWinRates.add("50.34%");
        mChampionPosition.add("Top | Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/reksai.png");
        mNames.add("Rek'Sai");
        mWinRates.add("50.44%");
        mChampionPosition.add("Jungle | Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/rengar.png");
        mNames.add("Rengar");
        mWinRates.add("50.44%");
        mChampionPosition.add("Jungle | Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/riven.png");
        mNames.add("Riven");
        mWinRates.add("50.44%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/rumble.png");
        mNames.add("Rumble");
        mWinRates.add("50.44%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/ryze.png");
        mNames.add("Ryze");
        mWinRates.add("50.44%");
        mChampionPosition.add("Jungle | Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/sejuani.png");
        mNames.add("Sejuani");
        mWinRates.add("50.44%");
        mChampionPosition.add("Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/shaco.png");
        mNames.add("Shaco");
        mWinRates.add("50.44%");
        mChampionPosition.add("Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/shen.png");
        mNames.add("Shen");
        mWinRates.add("50.44%");
        mChampionPosition.add("Top | Support");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/shyvana.png");
        mNames.add("Shyvana");
        mWinRates.add("50.44%");
        mChampionPosition.add("Jungle | Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/singed.png");
        mNames.add("Singed");
        mWinRates.add("50.44%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/sion.png");
        mNames.add("Sion");
        mWinRates.add("50.44%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/sivir.png");
        mNames.add("Sivir");
        mWinRates.add("50.44%");
        mChampionPosition.add("ADC");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/skarner.png");
        mNames.add("Skarner");
        mWinRates.add("50.44%");
        mChampionPosition.add("Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/sona.png");
        mNames.add("Sona");
        mWinRates.add("50.44%");
        mChampionPosition.add("Support");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/soraka.png");
        mNames.add("Soraka");
        mWinRates.add("50.44%");
        mChampionPosition.add("Support");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/swain.png");
        mNames.add("Swain");
        mWinRates.add("50.44%");
        mChampionPosition.add("Middle | Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/syndra.png");
        mNames.add("Syndra");
        mWinRates.add("50.44%");
        mChampionPosition.add("Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/tahm-kench.png");
        mNames.add("Tahm Kench");
        mWinRates.add("50.44%");
        mChampionPosition.add("Support");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/taliyah.png");
        mNames.add("Taliyah");
        mWinRates.add("50.44%");
        mChampionPosition.add("Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/talon.png");
        mNames.add("Talon");
        mWinRates.add("50.44%");
        mChampionPosition.add("Middle | Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/taric.png");
        mNames.add("Taric");
        mWinRates.add("50.44%");
        mChampionPosition.add("Support");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/teemo.png");
        mNames.add("Teemo");
        mWinRates.add("50.44%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/thresh.png");
        mNames.add("Thresh");
        mWinRates.add("50.44%");
        mChampionPosition.add("Support");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/tristana.png");
        mNames.add("Tristana");
        mWinRates.add("50.44%");
        mChampionPosition.add("ADC");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/trundle.png");
        mNames.add("Trundle");
        mWinRates.add("50.44%");
        mChampionPosition.add("Jungle | Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/tryndamere.png");
        mNames.add("Tryndamere");
        mWinRates.add("47.74%");
        mChampionPosition.add("Top | Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/twisted-fate.png");
        mNames.add("Twisted Fate");
        mWinRates.add("50.44%");
        mChampionPosition.add("Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/twitch.png");
        mNames.add("Twitch");
        mWinRates.add("50.44%");
        mChampionPosition.add("ADC");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/udyr.png");
        mNames.add("Udyr");
        mWinRates.add("50.44%");
        mChampionPosition.add("Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/urgot.png");
        mNames.add("Urgot");
        mWinRates.add("50.44%");
        mChampionPosition.add("TOP");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/varus.png");
        mNames.add("Varus");
        mWinRates.add("50.44%");
        mChampionPosition.add("ADC");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/vayne.png");
        mNames.add("Vayne");
        mWinRates.add("50.44%");
        mChampionPosition.add("ADC | Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/veigar.png");
        mNames.add("Veigar");
        mWinRates.add("50.44%");
        mChampionPosition.add("Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/velkoz.png");
        mNames.add("Vel'Koz");
        mWinRates.add("50.44%");
        mChampionPosition.add("Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/vi.png");
        mNames.add("Vi");
        mWinRates.add("50.44%");
        mChampionPosition.add("Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/viktor.png");
        mNames.add("Viktor");
        mWinRates.add("50.44%");
        mChampionPosition.add("Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/vladimir.png");
        mNames.add("Vladimir");
        mWinRates.add("50.44%");
        mChampionPosition.add("Top | Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/volibear.png");
        mNames.add("Volibear");
        mWinRates.add("50.44%");
        mChampionPosition.add("Jungle | Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/warwick.png");
        mNames.add("Warwick");
        mWinRates.add("50.44%");
        mChampionPosition.add("Jungle | Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/wukong.png");
        mNames.add("Wukong");
        mWinRates.add("50.44%");
        mChampionPosition.add("Top | Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/xayah.png");
        mNames.add("Xayah");
        mWinRates.add("50.44%");
        mChampionPosition.add("ADC");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/xerath.png");
        mNames.add("Xerath");
        mWinRates.add("50.44%");
        mChampionPosition.add("Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/xin-zhao.png");
        mNames.add("Xin Zhao");
        mWinRates.add("50.44%");
        mChampionPosition.add("Jungle | Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/yasuo.png");
        mNames.add("Yasuo");
        mWinRates.add("50.44%");
        mChampionPosition.add("Middle | Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/yorick.png");
        mNames.add("Yorick");
        mWinRates.add("50.44%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/zac.png");
        mNames.add("Zac");
        mWinRates.add("50.44%");
        mChampionPosition.add("Jungle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/zed.png");
        mNames.add("Zed");
        mWinRates.add("50.44%");
        mChampionPosition.add("Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/ziggs.png");
        mNames.add("Ziggs");
        mWinRates.add("50.44%");
        mChampionPosition.add("Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/zilean.png");
        mNames.add("Zilean");
        mWinRates.add("50.44%");
        mChampionPosition.add("Support | Middle");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/zoe.png");
        mNames.add("Zoe");
        mWinRates.add("39.28%");
        mChampionPosition.add("Mid | Sup");

        mImageUrls.add("https://www.mobafire.com/images/champion/icon/zyra.png");
        mNames.add("Zyra");
        mWinRates.add("50.44%");
        mChampionPosition.add("Support");

        initRecyclerView();
    }

    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: initialized RecyclerView");
        RecyclerView recyclerView = getActivity().findViewById(R.id.winrecyclerview);
        HardcodedChampionListAdapter adapter = new HardcodedChampionListAdapter(getActivity(), mNames, mImageUrls, mWinRates, mChampionPosition);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }
}
