package com.norbertotaveras.game_companion_app.ChampionPage;

import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import com.norbertotaveras.game_companion_app.ChampionPage.Retrofit.ChampionGGService;
import com.norbertotaveras.game_companion_app.ChampionPage.Retrofit.ChampionRiotAPI;
import com.norbertotaveras.game_companion_app.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainChampions extends Fragment {

    private View view;
    Button btnTop;
    RecyclerView championList;

    private static final String TAG = "ChampionsWinRate";
    private static final String BASE_URL = "http://api.champion.gg/v2/";
    private static final String RIOT_URL = "https://na1.api.riotgames.com/";

    FloatingActionButton fabPlus, fabTop, fabJun, fabMid, fabSup, fabBot, fabFilter;
    FloatingActionButton[] fabList;
    Animation FabOpen, FabClose, FabRotateClockWise, FabRotateCounterClockWise;
    boolean isOpen = false;
    int wantedPosition = 0; //0=all | 1=top | 2=jungle | 3=middle | 4=support | 5=bottom

    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> mWinRates = new ArrayList<>(); //Needs api calls, use placeholders atm
    private ArrayList<String> mChampionPosition = new ArrayList<>(); //
    private List<ChampionRates> rates;
    private String championName;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_main_champions, container, false);
        setHasOptionsMenu(true);

        //Sorting buttons
        fabPlus = view.findViewById(R.id.fab_plus);
        fabTop = view.findViewById(R.id.fab_top);
        fabJun = view.findViewById(R.id.fab_jungle);
        fabMid = view.findViewById(R.id.fab_middle);
        fabSup = view.findViewById(R.id.fab_support);
        fabBot = view.findViewById(R.id.fab_bottom);
        fabFilter = view.findViewById(R.id.fab_filter);
        FabOpen = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        FabClose = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);
        FabRotateClockWise = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_clockwise);
        FabRotateCounterClockWise = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_counterclockwise);

        fabList = new FloatingActionButton[] {
                fabTop, fabJun, fabMid, fabSup, fabBot, fabFilter
        };

        btnTop = view.findViewById(R.id.topButton);
        championList = view.findViewById(R.id.champ_list);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();

        ChampionGGService client = retrofit.create(ChampionGGService.class);
        Call<List<ChampionRates>> call = client.getChampInfo();

        Log.d(TAG, "About to Call on Championgg");

        call.enqueue(new Callback<List<ChampionRates>>() {
            @Override
            public void onResponse(Call<List<ChampionRates>> call, Response<List<ChampionRates>> response) {
                rates = response.body();

                Log.d(TAG, "Call worked");
                for (int i = 0; i < rates.size() - 1; i++)
                {
                    int currentID = Integer.parseInt(rates.get(i).getChampionId());
                    IDtoName(currentID);
                    rates.get(i).setChampionId(championName);

                    Log.v(TAG, "ChampID(" + i + ") = " + rates.get(i).getChampionId());
                    Log.v(TAG, "winRate(" + i + ") = " + rates.get(i).getWinRate());
                }

            }

            @Override
            public void onFailure(Call<List<ChampionRates>> call, Throwable t) {
                Log.e(TAG, "Unable to retrieve from champion.gg");
                Toast.makeText(getActivity(), "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });


        Log.d(TAG, "onCreate: starting.");



        btnTop.setEnabled(false);

        initImageBitmaps(wantedPosition, championList);

        fabPlus.setOnClickListener(new View.OnClickListener() { //Sorting buttons
            @Override
            public void onClick(View v) {
                fabToggle();
            }
        });

        fabFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabToggle();

                mImageUrls.clear();
                mNames.clear();
                mWinRates.clear();
                mChampionPosition.clear();
                wantedPosition = 0;
                Log.i(TAG, "User picked Filter");
                initImageBitmaps(wantedPosition, championList);
            }
        });

        fabTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                fabToggle();

                mImageUrls.clear();
                mNames.clear();
                mWinRates.clear();
                mChampionPosition.clear();
                wantedPosition = 1;
                Log.i(TAG, "User picked Top");
                initImageBitmaps(wantedPosition, championList);
            }
        });

        fabJun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabToggle();

                mImageUrls.clear();
                mNames.clear();
                mWinRates.clear();
                mChampionPosition.clear();
                wantedPosition = 2;
                Log.i(TAG, "User picked Jungle");
                initImageBitmaps(wantedPosition, championList);
            }
        });

        fabMid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                fabToggle();

                mImageUrls.clear();
                mNames.clear();
                mWinRates.clear();
                mChampionPosition.clear();
                wantedPosition = 3;
                Log.i(TAG, "User picked Middle");
                initImageBitmaps(wantedPosition, championList);
            }
        });

        fabSup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                fabToggle();

                mImageUrls.clear();
                mNames.clear();
                mWinRates.clear();
                mChampionPosition.clear();
                wantedPosition = 4;
                Log.i(TAG, "User picked Support");
                initImageBitmaps(wantedPosition, championList);
            }
        });

        fabBot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                fabToggle();

                mImageUrls.clear();
                mNames.clear();
                mWinRates.clear();
                mChampionPosition.clear();
                wantedPosition = 5;
                Log.i(TAG, "User picked Bottom");
                initImageBitmaps(wantedPosition, championList);
            }
        });

        championList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = ((LinearLayoutManager) championList.getLayoutManager());
                int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                btnTop.setEnabled(true);
                btnTop.setVisibility(View.VISIBLE);
                btnTop.setAlpha(0.50f);

                if (firstVisiblePosition > 1) // && ScrollAmount < dy // Scroll has to be less than current y in order for us to realize it's scrolling up
                {
                    btnTop.setEnabled(true);
                    buttonAnimation(btnTop);

                    btnTop.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            championList.getLayoutManager().scrollToPosition(0);
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
    }

    //For when connection to ChampionGG goes through
//    public void addChampion()
//    {
//        for (int i = 0; i < 147; i++)
//        {
//            mImageUrls.add(apicall.champimage[i]);
//            mNames.add(apicall.champnames[i]);
//            mWinRates.add(apicall.champwinrate[i]);
//            mChampionPosition.add(apicall.champposition[i]);
//        }
//    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mainchampmenu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.win_search:
                return true;
            case R.id.pick_search:
                return true;
            case R.id.ban_search:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void IDtoName(int id)
    {
        switch (id)
        {
            case 266:
                championName =  "Aatrox";
            case 412:
                championName =  "Thresh";
            case 23:
                championName = "Tryndamere";
            case 79:
                championName = "Gragas";
            case 69:
                championName = "Cassiopeia";
            case 136:
                championName = "Aurelion Sol";
            case 13:
                championName = "Ryze";
            case 78:
                championName = "Poppy";
            case 14:
                championName = "Sion";
            case 1:
                championName = "Annie";
            case 202:
                championName = "Jhin";
            case 43:
                championName = "Karma";
            case 111:
                championName = "Nautilus";
            case 240:
                championName = "Kled";
            case 99:
                championName = "Lux";
            case 103:
                championName = "Ahri";
            case 2:
                championName = "Olaf";
            case 112:
                championName = "Viktor";
            case 34:
                championName = "Anivia";
            case 27:
                championName = "Singed";
            case 86:
                championName = "Garen";
            case 127:
                championName = "Lissandra";
            case 57:
                championName = "Maokai";
            case 25:
                championName = "Morgana";
            case 28:
                championName = "Evelynn";
            case 105:
                championName = "Fizz";
            case 74:
                championName = "Heimerdinger";
            case 238:
                championName = "Zed";
            case 68:
                championName = "Rumble";
            case 82:
                championName = "Mordekaiser";
            case 37:
                championName = "Sona";
            case 96:
                championName = "Kog'Maw";
            case 55:
                championName = "Katarina";
            case 117:
                championName = "Lulu";
            case 22:
                championName = "Ashe";
            case 30:
                championName = "Karthus";
            case 12:
                championName = "Alistar";
            case 122:
                championName = "Darius";
            case 67:
                championName = "Vayne";
            case 110:
                championName = "Varus";
            case 77:
                championName = "Udyr";
            case 89:
                championName = "Leona";
            case 126:
                championName = "Jayce";
            case 134:
                championName = "Syndra";
            case 80:
                championName = "Pantheon";
            case 92:
                championName = "Riven";
            case 121:
                championName = "Kha'Zix";
            case 42:
                championName = "Corki";
            case 268:
                championName = "Azir";
            case 51:
                championName = "Caitlyn";
            case 76:
                championName = "Nidalee";
            case 85:
                championName = "Kennen";
            case 3:
                championName = "Galio";
            case 45:
                championName = "Veigar";
            case 432:
                championName = "Bard";
            case 150:
                championName = "Gnar";
            case 90:
                championName = "Malzahar";
            case 104:
                championName = "Graves";
            case 254:
                championName = "Vi";
            case 10:
                championName = "Kayle";
            case 39:
                championName = "Irelia";
            case 64:
                championName = "Lee Sin";
            case 420:
                championName = "Illaoi";
            case 60:
                championName = "Elise";
            case 106:
                championName = "Volibear";
            case 20:
                championName = "Nunu";
            case 4:
                championName = "Twisted Fate";
            case 24:
                championName = "Jax";
            case 102:
                championName = "Shyvana";
            case 429:
                championName = "Kalista";
            case 36:
                championName = "Dr. Mundo";
            case 427:
                championName = "Ivern";
            case 131:
                championName = "Diana";
            case 223:
                championName = "Tahm Kench";
            case 63:
                championName = "Brand";
            case 113:
                championName = "Sejuani";
            case 8:
                championName = "Vladimir";
            case 154:
                championName = "Zac";
            case 421:
                championName = "Rek'Sai";
            case 133:
                championName = "Quinn";
            case 84:
                championName = "Akali";
            case 163:
                championName = "Taliyah";
            case 18:
                championName = "Tristana";
            case 120:
                championName = "Hecarim";
            case 15:
                championName = "Sivir";
            case 236:
                championName = "Lucian";
            case 107:
                championName = "Rengar";
            case 19:
                championName = "Warwick";
            case 72:
                championName = "Skarner";
            case 54:
                championName = "Malphite";
            case 157:
                championName = "Yasuo";
            case 101:
                championName = "Xerath";
            case 17:
                championName = "Teemo";
            case 75:
                championName = "Nasus";
            case 58:
                championName = "Renekton";
            case 119:
                championName = "Draven";
            case 35:
                championName = "Shaco";
            case 50:
                championName = "Swain";
            case 91:
                championName = "Talon";
            case 40:
                championName = "Janna";
            case 115:
                championName = "Ziggs";
            case 245:
                championName = "Ekko";
            case 61:
                championName = "Orianna";
            case 114:
                championName = "Fiora";
            case 9:
                championName = "Fiddlesticks";
            case 31:
                championName = "Cho'Gath";
            case 33:
                championName = "Rammus";
            case 7:
                championName = "LeBlanc";
            case 16:
                championName = "Soraka";
            case 26:
                championName = "Zilean";
            case 56:
                championName = "Nocturne";
            case 222:
                championName = "Jinx";
            case 83:
                championName = "Yorick";
            case 6:
                championName = "Urgot";
            case 203:
                championName = "Kindred";
            case 21:
                championName = "Miss Fortune";
            case 62:
                championName = "Wukong";
            case 53:
                championName = "Blitzcrank";
            case 98:
                championName = "Shen";
            case 201:
                championName = "Braum";
            case 5:
                championName = "Xin Zhao";
            case 29:
                championName = "Twitch";
            case 11:
                championName = "Master Yi";
            case 44:
                championName = "Taric";
            case 32:
                championName = "Amumu";
            case 41:
                championName = "Gangplank";
            case 48:
                championName = "Trundle";
            case 38:
                championName = "Kassadin";
            case 161:
                championName = "Vel'Koz";
            case 143:
                championName = "Zyra";
            case 267:
                championName = "Nami";
            case 59:
                championName = "Jarvan IV";
            case 81:
                championName = "Ezreal";
        }
    }

    public void TransitionID2Name(int id) {
        final int champID = id;

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(RIOT_URL)
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();

        ChampionRiotAPI riotclient = retrofit.create(ChampionRiotAPI.class);
        Call<String> riotCall = riotclient.getChampionById(champID);

        riotCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                championName = response.body();
                Log.v(TAG, "ChampID: " + champID + " | ChampName: " + championName);
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
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

    public void clear() {
        final int size = mNames.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                mImageUrls.remove(0);
                mNames.remove(0);
                mWinRates.remove(0);
                mChampionPosition.remove(0);
            }
        }
    }


    public void fabToggle() {
        isOpen = !isOpen;

        Animation fabAnim = isOpen ? FabOpen : FabClose;
        Animation fabRotation = isOpen ? FabRotateClockWise : FabRotateCounterClockWise;

        for (FloatingActionButton fab : fabList) {
            fab.startAnimation(fabAnim);
            fab.setClickable(isOpen);
        }

        fabPlus.startAnimation(fabRotation);
    }

    private void initImageBitmaps(int wantedPosition, RecyclerView rView) {
        Log.d(TAG, "initImageBitmaps: preparing bitmaps.");

        //Call<List<ChampionDTO>> champions = apiService.getChampions();
        //Log.d(TAG, champions.toString());
        //Best way is to call them all, but how would I sort the info gained to it's specific champion?
        //Hardcode names, pictures, and positions but leave winrates to be dynamically allocated to champs.
        //LeagueAPI is down even for OPGG, hardcoding paths EXCEPT winrate until it works again

        mImageUrls.add("http://media.comicbook.com/2017/07/aatrox-0-1005633.jpg");
        mNames.add("Aatrox");
        mWinRates.add("54.76");
        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/ahri.png");
//        mNames.add("Ahri");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/avatars/akali-classic.png");
//        mNames.add("Akali");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/rectangle/alistar.png");
//        mNames.add("Alistar");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Support");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/amumu.png");
//        mNames.add("Amumu");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/anivia.png");
//        mNames.add("Anivia");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/annie.png");
//        mNames.add("Annie");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Middle | Support");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/ashe.png");
//        mNames.add("Ashe");
//        mWinRates.add("54.76");
//        mChampionPosition.add("ADC");
//
//        mImageUrls.add("https://www.mobafire.com/images/avatars/aurelion-sol-classic.png");
//        mNames.add("Aurelion Sol");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/square/azir.png");
//        mNames.add("Azir");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/bard.png");
//        mNames.add("Bard");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Support");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/blitzcrank.png");
//        mNames.add("Blitzcrank");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Support");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/brand.png");
//        mNames.add("Brand");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Middle | Support");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/braum.png");
//        mNames.add("Braum");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Support");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/caitlyn.png");
//        mNames.add("Caitlyn");
//        mWinRates.add("54.76");
//        mChampionPosition.add("ADC");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/camille.png");
//        mNames.add("Camille");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/cassiopeia.png");
//        mNames.add("Cassiopeia");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/chogath.png");
//        mNames.add("Cho'Gath");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Middle | Top | Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/corki.png");
//        mNames.add("Corki");
//        mWinRates.add("54.76");
//        mChampionPosition.add("ADC | Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/darius.png");
//        mNames.add("Darius");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/diana.png");
//        mNames.add("Diana");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Middle | Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/dr-mundo.png");
//        mNames.add("Dr. Mundo");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/draven.png");
//        mNames.add("Draven");
//        mWinRates.add("54.76");
//        mChampionPosition.add("ADC");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/ekko.png");
//        mNames.add("Ekko");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Middle | Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/elise.png");
//        mNames.add("Elise");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/evelynn.png");
//        mNames.add("Evelynn");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/ezreal.png");
//        mNames.add("Ezreal");
//        mWinRates.add("54.76");
//        mChampionPosition.add("ADC");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/fiddlesticks.png");
//        mNames.add("Fiddlesticks");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Middle | Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/square/fiora.png");
//        mNames.add("Fiora");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/fizz.png");
//        mNames.add("Fizz");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/galio.png");
//        mNames.add("Galio");
//        mWinRates.add("54.76");
//        mChampionPosition.add("Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/gangplank.png");
//        mNames.add("Gangplank");
//        mWinRates.add("53.13");
//        mChampionPosition.add("Top | Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/garen.png");
//        mNames.add("Garen");
//        mWinRates.add("53.13");
//        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/gnar.png");
//        mNames.add("Gnar");
//        mWinRates.add("53.13");
//        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/gragas.png");
//        mNames.add("Gragas");
//        mWinRates.add("53.13");
//        mChampionPosition.add("Jungle | Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/graves.png");
//        mNames.add("Graves");
//        mWinRates.add("53.13");
//        mChampionPosition.add("ADC | Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/hecarim.png");
//        mNames.add("Hecarim");
//        mWinRates.add("53.13");
//        mChampionPosition.add("Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/heimerdinger.png");
//        mNames.add("Heimerdinger");
//        mWinRates.add("53.13");
//        mChampionPosition.add("Top | Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/illaoi.png");
//        mNames.add("Illaoi");
//        mWinRates.add("53.13");
//        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/irelia.png");
//        mNames.add("Irelia");
//        mWinRates.add("53.13");
//        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/ivern.png");
//        mNames.add("Ivern");
//        mWinRates.add("53.13");
//        mChampionPosition.add("Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/janna.png");
//        mNames.add("Janna");
//        mWinRates.add("53.13");
//        mChampionPosition.add("Support");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/jarvan-iv.png");
//        mNames.add("Jarvan IV");
//        mWinRates.add("53.13");
//        mChampionPosition.add("Top | Jungle");
//
//        mImageUrls.add("http://www.behindthevoiceactors.com/_img/chars/jax-league-of-legends-4.27.jpg");
//        mNames.add("Jax");
//        mWinRates.add("50.89");
//        mChampionPosition.add("Top | Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/jayce.png");
//        mNames.add("Jayce");
//        mWinRates.add("53.13");
//        mChampionPosition.add("Top | Mid");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/jhin.png");
//        mNames.add("Jhin");
//        mWinRates.add("53.13");
//        mChampionPosition.add("ADC");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/jinx.png");
//        mNames.add("Jinx");
//        mWinRates.add("53.13");
//        mChampionPosition.add("ADC");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/kalista.png");
//        mNames.add("Kalista");
//        mWinRates.add("53.13");
//        mChampionPosition.add("ADC");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/karma.png");
//        mNames.add("Karma");
//        mWinRates.add("53.13");
//        mChampionPosition.add("Support | Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/karthus.png");
//        mNames.add("Karthus");
//        mWinRates.add("52.94");
//        mChampionPosition.add("Mid");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/kassadin.png");
//        mNames.add("Kassadin");
//        mWinRates.add("52.94");
//        mChampionPosition.add("Mid");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/katarina.png");
//        mNames.add("Katarina");
//        mWinRates.add("52.94");
//        mChampionPosition.add("Mid");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/kayle.png");
//        mNames.add("Kayle");
//        mWinRates.add("52.94");
//        mChampionPosition.add("Mid | Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/avatars/kayn-classic.png");
//        mNames.add("Kayn");
//        mWinRates.add("49.73");
//        mChampionPosition.add("Jungle | Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/kennen.png");
//        mNames.add("Kennen");
//        mWinRates.add("52.94");
//        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/khazix.png");
//        mNames.add("Kha'Zix");
//        mWinRates.add("52.94");
//        mChampionPosition.add("Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/kindred.png");
//        mNames.add("Kindred");
//        mWinRates.add("52.94");
//        mChampionPosition.add("Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/kled.png");
//        mNames.add("Kled");
//        mWinRates.add("52.94");
//        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/kogmaw.png");
//        mNames.add("Kog'Maw");
//        mWinRates.add("52.94");
//        mChampionPosition.add("ADC | Mid");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/leblanc.png");
//        mNames.add("LeBlanc");
//        mWinRates.add("52.94");
//        mChampionPosition.add("Mid");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/lee-sin.png");
//        mNames.add("Lee Sin");
//        mWinRates.add("52.94");
//        mChampionPosition.add("Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/leona.png");
//        mNames.add("Leona");
//        mWinRates.add("52.94");
//        mChampionPosition.add("Support");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/lissandra.png");
//        mNames.add("Lissandra");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/lucian.png");
//        mNames.add("Lucian");
//        mWinRates.add("43.58");
//        mChampionPosition.add("ADC");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/lulu.png");
//        mNames.add("Lulu");
//        mWinRates.add("38.28");
//        mChampionPosition.add("Support");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/lux.png");
//        mNames.add("Lux");
//        mWinRates.add("38.23");
//        mChampionPosition.add("Mid | Support");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/malphite.png");
//        mNames.add("Malphite");
//        mWinRates.add("39.48");
//        mChampionPosition.add("Top | Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/malzahar.png");
//        mNames.add("Malzahar");
//        mWinRates.add("15.87");
//        mChampionPosition.add("Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/maokai.png");
//        mNames.add("Maokai");
//        mWinRates.add("24.34");
//        mChampionPosition.add("Top | Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/master-yi.png");
//        mNames.add("Master Yi");
//        mWinRates.add("32.68");
//        mChampionPosition.add("Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/miss-fortune.png");
//        mNames.add("Miss Fortune");
//        mWinRates.add("40.72");
//        mChampionPosition.add("ADC | Sup");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/mordekaiser.png");
//        mNames.add("Mordekaiser");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/morgana.png");
//        mNames.add("Morgana");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Support | Mid");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/nami.png");
//        mNames.add("Nami");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Support");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/nasus.png");
//        mNames.add("Nasus");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/nautilus.png");
//        mNames.add("Nautilus");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Jungle | Support");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/nidalee.png");
//        mNames.add("Nidalee");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Jungle | Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/nocturne.png");
//        mNames.add("Nocturne");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/nunu.png");
//        mNames.add("Nunu");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Jungle | Support");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/olaf.png");
//        mNames.add("Olaf");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Top | Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/orianna.png");
//        mNames.add("Orianna");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/ornn.png");
//        mNames.add("Ornn");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/square/pantheon.png");
//        mNames.add("Pantheon");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/poppy.png");
//        mNames.add("Poppy");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/quinn.png");
//        mNames.add("Quinn");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Top | ADC");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/rakan.png");
//        mNames.add("Rakan");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Support");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/rammus.png");
//        mNames.add("Rammus");
//        mWinRates.add("50.34");
//        mChampionPosition.add("Top | Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/reksai.png");
//        mNames.add("Rek'Sai");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Jungle | Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/rengar.png");
//        mNames.add("Rengar");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Jungle | Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/riven.png");
//        mNames.add("Riven");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/rumble.png");
//        mNames.add("Rumble");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/ryze.png");
//        mNames.add("Ryze");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Middle | Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/sejuani.png");
//        mNames.add("Sejuani");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/shaco.png");
//        mNames.add("Shaco");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/shen.png");
//        mNames.add("Shen");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Top | Support");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/shyvana.png");
//        mNames.add("Shyvana");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Jungle | Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/singed.png");
//        mNames.add("Singed");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/sion.png");
//        mNames.add("Sion");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/sivir.png");
//        mNames.add("Sivir");
//        mWinRates.add("50.44");
//        mChampionPosition.add("ADC");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/skarner.png");
//        mNames.add("Skarner");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/sona.png");
//        mNames.add("Sona");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Support");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/soraka.png");
//        mNames.add("Soraka");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Support");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/swain.png");
//        mNames.add("Swain");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Middle | Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/syndra.png");
//        mNames.add("Syndra");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/tahm-kench.png");
//        mNames.add("Tahm Kench");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Support");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/taliyah.png");
//        mNames.add("Taliyah");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/talon.png");
//        mNames.add("Talon");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Middle | Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/taric.png");
//        mNames.add("Taric");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Support");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/teemo.png");
//        mNames.add("Teemo");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/thresh.png");
//        mNames.add("Thresh");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Support");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/tristana.png");
//        mNames.add("Tristana");
//        mWinRates.add("50.44");
//        mChampionPosition.add("ADC");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/trundle.png");
//        mNames.add("Trundle");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Jungle | Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/tryndamere.png");
//        mNames.add("Tryndamere");
//        mWinRates.add("47.74");
//        mChampionPosition.add("Top | Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/twisted-fate.png");
//        mNames.add("Twisted Fate");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/twitch.png");
//        mNames.add("Twitch");
//        mWinRates.add("50.44");
//        mChampionPosition.add("ADC");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/udyr.png");
//        mNames.add("Udyr");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/urgot.png");
//        mNames.add("Urgot");
//        mWinRates.add("50.44");
//        mChampionPosition.add("TOP");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/varus.png");
//        mNames.add("Varus");
//        mWinRates.add("50.44");
//        mChampionPosition.add("ADC");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/vayne.png");
//        mNames.add("Vayne");
//        mWinRates.add("50.44");
//        mChampionPosition.add("ADC | Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/veigar.png");
//        mNames.add("Veigar");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/velkoz.png");
//        mNames.add("Vel'Koz");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/vi.png");
//        mNames.add("Vi");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/viktor.png");
//        mNames.add("Viktor");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/vladimir.png");
//        mNames.add("Vladimir");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Top | Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/volibear.png");
//        mNames.add("Volibear");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Jungle | Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/warwick.png");
//        mNames.add("Warwick");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Jungle | Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/wukong.png");
//        mNames.add("Wukong");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Top | Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/xayah.png");
//        mNames.add("Xayah");
//        mWinRates.add("23.44");
//        mChampionPosition.add("ADC");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/xerath.png");
//        mNames.add("Xerath");
//        mWinRates.add("56.44");
//        mChampionPosition.add("Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/xin-zhao.png");
//        mNames.add("Xin Zhao");
//        mWinRates.add("24.44");
//        mChampionPosition.add("Jungle | Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/yasuo.png");
//        mNames.add("Yasuo");
//        mWinRates.add("55.44");
//        mChampionPosition.add("Middle | Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/yorick.png");
//        mNames.add("Yorick");
//        mWinRates.add("50.44");
//        mChampionPosition.add("Top");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/zac.png");
//        mNames.add("Zac");
//        mWinRates.add("23.44");
//        mChampionPosition.add("Jungle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/zed.png");
//        mNames.add("Zed");
//        mWinRates.add("50.22");
//        mChampionPosition.add("Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/ziggs.png");
//        mNames.add("Ziggs");
//        mWinRates.add("10.44");
//        mChampionPosition.add("Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/zilean.png");
//        mNames.add("Zilean");
//        mWinRates.add("53.44");
//        mChampionPosition.add("Support | Middle");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/zoe.png");
//        mNames.add("Zoe");
//        mWinRates.add("29.28");
//        mChampionPosition.add("Middle | Support");
//
//        mImageUrls.add("https://www.mobafire.com/images/champion/icon/zyra.png");
//        mNames.add("Zyra");
//        mWinRates.add("10.42");
//        mChampionPosition.add("Support | Middle");

        int m = mWinRates.size();

        switch (wantedPosition)
        {
            case 0:
                break;
            case 1:
                for (int i = 0; i < m-1; i++ )
                {
                    if (i == 147) { break; }

                    if (!mChampionPosition.get(i).toLowerCase().contains("top"))
                    {
                        mImageUrls.remove(i);
                        mNames.remove(i);
                        mWinRates.remove(i);
                        mChampionPosition.remove(i);
                        m--;
                        i--;
                    }
                }
                break;

            case 2:
                for (int i = 0; i < m-1; i++ )
                {
                    if (i == 147) { break; }

                    if (!mChampionPosition.get(i).toLowerCase().contains("jun"))
                    {
                        mImageUrls.remove(i);
                        mNames.remove(i);
                        mWinRates.remove(i);
                        mChampionPosition.remove(i);
                        m--;
                        i--;
                    }
                }
                break;

            case 3:
                for (int i = 0; i < m-1; i++ )
                {
                    if (i == 147) { break; }

                    if (!mChampionPosition.get(i).toLowerCase().contains("mid"))
                    {
                        mImageUrls.remove(i);
                        mNames.remove(i);
                        mWinRates.remove(i);
                        mChampionPosition.remove(i);
                        m--;
                        i--;
                    }
                }
                break;

            case 4:
                for (int i = 0; i < m-1; i++ )
                {
                    if (i == 147) { break; }

                    if (!mChampionPosition.get(i).toLowerCase().contains("sup"))
                    {
                        mImageUrls.remove(i);
                        mNames.remove(i);
                        mWinRates.remove(i);
                        mChampionPosition.remove(i);
                        m--;
                        i--;
                    }
                }
                break;

            case 5:

                for (int i = 0; i < m-1; i++ )
                {
                    if (i == 147) { break; }

                    if (!mChampionPosition.get(i).toLowerCase().contains("adc"))
                    {
                        mImageUrls.remove(i);
                        mNames.remove(i);
                        mWinRates.remove(i);
                        mChampionPosition.remove(i);
                        m--;
                        i--;
                        Log.v(TAG, String.valueOf(mWinRates.size()));
                    }
                }
                break;

            default:
                break;

        }

        int n = mWinRates.size();

        //Good ol' bubblesort!
        for (int i = 0; i < n - 1; i++)
        {
            for (int j = 0; j < n-i-1; j++)
            {
                if (Float.valueOf(mWinRates.get(j)) < Float.valueOf(mWinRates.get(j+1)))
                {
                    int rep = j+1;

                    String tempURL = mImageUrls.get(j);
                    mImageUrls.set(j, mImageUrls.get(rep));
                    mImageUrls.set(rep, tempURL);

                    String tempName = mNames.get(j);
                    mNames.set(j, mNames.get(rep));
                    mNames.set(rep, tempName);

                    String tempRate = mWinRates.get(j);
                    mWinRates.set(j, mWinRates.get(rep));
                    mWinRates.set(rep, tempRate);

                    String tempPosition = mChampionPosition.get(j);
                    mChampionPosition.set(j, mChampionPosition.get(rep));
                    mChampionPosition.set(rep, tempPosition);

                }
            }

        }

        initChampList(rView);
    }

    private void initChampList(RecyclerView rView){
        Log.d(TAG, "initChampList: initialized RecyclerView");
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(getContext(), mNames, mImageUrls, mWinRates, mChampionPosition);
        rView.setAdapter(adapter);
        rView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public static Fragment newInstance() {
        return new MainChampions();
    }
}
