package com.norbertotaveras.game_companion_app.ChampionPage;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import com.norbertotaveras.game_companion_app.ChampionPage.Retrofit.RetroClasses.ChampionRates;
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
    int wantedRating = 0; //0=all | 1=win | 2=pick | 3=ban

    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> mWinRates = new ArrayList<>(); //Needs api calls, use placeholders atm
    private ArrayList<String> mChampionPosition = new ArrayList<>(); //
    private ArrayList<String> mRankPosition = new ArrayList<>();
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

        //ChampionGG stuff
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

                initImageBitmaps(wantedPosition, championList);

            }

            @Override
            public void onFailure(Call<List<ChampionRates>> call, Throwable t) {
                Log.e(TAG, "Unable to retrieve from champion.gg");
                Toast.makeText(getActivity(), "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });

        btnTop.setEnabled(false);


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
                mRankPosition.clear();
                wantedPosition = 0;
                Log.i(TAG, "User picked Filter");
                initImageBitmaps(wantedPosition, championList);
                Toast.makeText(getActivity(), "All Lanes", Toast.LENGTH_SHORT).show();
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
                mRankPosition.clear();
                wantedPosition = 1;
                Log.i(TAG, "User picked Top");
                initImageBitmaps(wantedPosition, championList);
                Toast.makeText(getActivity(), "Top", Toast.LENGTH_SHORT).show();
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
                mRankPosition.clear();
                wantedPosition = 2;
                Log.i(TAG, "User picked Jungle");
                initImageBitmaps(wantedPosition, championList);
                Toast.makeText(getActivity(), "Jungle", Toast.LENGTH_SHORT).show();
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
                mRankPosition.clear();
                wantedPosition = 3;
                Log.i(TAG, "User picked Middle");
                initImageBitmaps(wantedPosition, championList);
                Toast.makeText(getActivity(), "Mid", Toast.LENGTH_SHORT).show();
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
                mRankPosition.clear();
                wantedPosition = 4;
                Log.i(TAG, "User picked Support");
                initImageBitmaps(wantedPosition, championList);
                Toast.makeText(getActivity(), "Support", Toast.LENGTH_SHORT).show();
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
                mRankPosition.clear();
                wantedPosition = 5;
                Log.i(TAG, "User picked Bottom");
                initImageBitmaps(wantedPosition, championList);
                Toast.makeText(getActivity(), "ADC", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.mainchampmenu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.win_search:
                wantedRating = 0;
                initImageBitmaps(wantedPosition, championList);
                Toast.makeText(getActivity(), "WinRate", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.pick_search:
                wantedRating = 1;
                initImageBitmaps(wantedPosition, championList);
                Toast.makeText(getActivity(), "PickRate", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.ban_search:
                wantedRating = 2;
                initImageBitmaps(wantedPosition, championList);
                Toast.makeText(getActivity(), "BanRate", Toast.LENGTH_SHORT).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void IDtoName(int id)
    {
        switch (id)
        {
            case 266:
                championName =  "Aatrox"; // //173jung
                break;
            case 412:
                championName =  "Thresh"; //
                break;
            case 23:
                championName = "Tryndamere"; //118top //174jung
                break;
            case 79:
                championName = "Gragas"; //135jung //183mid
                break;
            case 69:
                championName = "Cassiopeia"; //
                break;
            case 136:
                championName = "Aurelion Sol"; //
                break;
            case 13:
                championName = "Ryze"; //
                break;
            case 78:
                championName = "Poppy"; //120top //178jung //179supp
                break;
            case 14:
                championName = "Sion"; // //126supp
                break;
            case 1:
                championName = "Annie"; //129mid //176supp
                break;
            case 202:
                championName = "Jhin"; //
                break;
            case 43:
                championName = "Karma"; //62supp //159mid
                break;
            case 111:
                championName = "Nautilus"; //130supp //175top
                break;
            case 240:
                championName = "Kled"; //
                break;
            case 99:
                championName = "Lux"; // //112supp
                break;
            case 103:
                championName = "Ahri"; //
                break;
            case 2:
                championName = "Olaf"; //161top //165jung
                break;
            case 112:
                championName = "Viktor"; //
                break;
            case 34:
                championName = "Anivia"; //
                break;
            case 27:
                championName = "Singed"; //
                break;
            case 86:
                championName = "Garen"; //
                break;
            case 145:
                championName = "Kai'Sa"; //New Champ 95jung 11adc
                break;
            case 127:
                championName = "Lissandra"; // //184top
                break;
            case 57:
                championName = "Maokai"; // //167jung
                break;
            case 25:
                championName = "Morgana"; // //45mid
                break;
            case 28:
                championName = "Evelynn"; //
                break;
            case 105:
                championName = "Fizz"; //
                break;
            case 74:
                championName = "Heimerdinger"; //164top //170mid
                break;
            case 238:
                championName = "Zed"; //
                break;
            case 68:
                championName = "Rumble"; //136top
                break;
            case 82:
                championName = "Mordekaiser"; // //187mid
                break;
            case 37:
                championName = "Sona"; //
                break;
            case 96:
                championName = "Kog'Maw"; //
                break;
            case 55:
                championName = "Katarina"; //
                break;
            case 117:
                championName = "Lulu"; //
                break;
            case 22:
                championName = "Ashe"; //
                break;
            case 30:
                championName = "Karthus"; // //185top
                break;
            case 12:
                championName = "Alistar"; //
                break;
            case 122:
                championName = "Darius"; //
                break;
            case 67:
                championName = "Vayne"; //
                break;
            case 110:
                championName = "Varus"; //
                break;
            case 77:
                championName = "Udyr"; //
                break;
            case 89:
                championName = "Leona"; //
                break;
            case 126:
                championName = "Jayce"; //111top //163mid
                break;
            case 134:
                championName = "Syndra"; //
                break;
            case 80:
                championName = "Pantheon"; // //158jung
                break;
            case 92:
                championName = "Riven"; //
                break;
            case 121:
                championName = "Kha'Zix"; //
                break;
            case 42:
                championName = "Corki"; //65mid
                break;
            case 268:
                championName = "Azir"; //
                break;
            case 51:
                championName = "Caitlyn"; //
                break;
            case 76:
                championName = "Nidalee"; //70jung
                break;
            case 85:
                championName = "Kennen"; //149top //186mid
                break;
            case 3:
                championName = "Galio"; //mid //155top
                break;
            case 45:
                championName = "Veigar"; // //157supp
                break;
            case 432:
                championName = "Bard"; //
                break;
            case 150:
                championName = "Gnar"; //
                break;
            case 90:
                championName = "Malzahar"; //
                break;
            case 104:
                championName = "Graves"; //125jung
                break;
            case 254:
                championName = "Vi"; //
                break;
            case 10:
                championName = "Kayle"; //153top //180mid
                break;
            case 39:
                championName = "Irelia"; //
                break;
            case 64:
                championName = "Lee Sin"; //
                break;
            case 420:
                championName = "Illaoi"; //
                break;
            case 60:
                championName = "Elise"; //
                break;
            case 106:
                championName = "Volibear"; // //127top
                break;
            case 20:
                championName = "Nunu"; //106jung //
                break;
            case 4:
                championName = "Twisted Fate"; //
                break;
            case 24:
                championName = "Jax"; // //69top
                break;
            case 102:
                championName = "Shyvana"; //
                break;
            case 429:
                championName = "Kalista"; //
                break;
            case 36:
                championName = "Dr. Mundo"; //147top //181jung
                break;
            case 427:
                championName = "Ivern"; //151jung
                break;
            case 131:
                championName = "Diana"; //107mid //168jung
                break;
            case 223:
                championName = "Tahm Kench"; //63Supp //160top
                break;
            case 63:
                championName = "Brand"; //support //119mid
                break;
            case 113:
                championName = "Sejuani"; //
                break;
            case 8:
                championName = "Vladimir"; //81 mid //90top
                break;
            case 154:
                championName = "Zac"; //
                break;
            case 421:
                championName = "Rek'Sai"; //
                break;
            case 133:
                championName = "Quinn"; //169top //188adc //189jung
                break;
            case 84:
                championName = "Akali"; //131top //140mid
                break;
            case 163:
                championName = "Taliyah"; //
                break;
            case 18:
                championName = "Tristana"; //
                break;
            case 120:
                championName = "Hecarim"; //
                break;
            case 15:
                championName = "Sivir"; //
                break;
            case 236:
                championName = "Lucian"; //
                break;
            case 107:
                championName = "Rengar"; // //117top
                break;
            case 19:
                championName = "Warwick"; //
                break;
            case 72:
                championName = "Skarner"; //
                break;
            case 54:
                championName = "Malphite"; // //166supp
                break;
            case 157:
                championName = "Yasuo"; //mid//top61
                break;
            case 101:
                championName = "Xerath"; // //150supp
                break;
            case 17:
                championName = "Teemo"; // //171supp
                break;
            case 75:
                championName = "Nasus"; //
                break;
            case 58:
                championName = "Renekton"; //
                break;
            case 119:
                championName = "Draven"; //
                break;
            case 35:
                championName = "Shaco"; //
                break;
            case 50:
                championName = "Swain"; // //97mid
                break;
            case 91:
                championName = "Talon"; //
                break;
            case 40:
                championName = "Janna"; //
                break;
            case 115:
                championName = "Ziggs"; //
                break;
            case 245:
                championName = "Ekko"; // //156jung
                break;
            case 61:
                championName = "Orianna"; //
                break;
            case 114:
                championName = "Fiora"; //
                break;
            case 9:
                championName = "Fiddlesticks"; // //105jung //139supp
                break;
            case 31:
                championName = "Cho'Gath"; // //172jung
                break;
            case 33:
                championName = "Rammus"; //
                break;
            case 7:
                championName = "LeBlanc"; //
                break;
            case 16:
                championName = "Soraka"; //
                break;
            case 26:
                championName = "Zilean"; //109supp //154mid
                break;
            case 56:
                championName = "Nocturne"; //141jung
                break;
            case 222:
                championName = "Jinx"; //
                break;
            case 83:
                championName = "Yorick"; //
                break;
            case 6:
                championName = "Urgot"; //
                break;
            case 203:
                championName = "Kindred"; //
                break;
            case 21:
                championName = "Miss Fortune"; //
                break;
            case 62:
                championName = "Wukong"; //142jung //152top
                break;
            case 53:
                championName = "Blitzcrank"; //
                break;
            case 98:
                championName = "Shen"; //82top //100sup
                break;
            case 201:
                championName = "Braum"; //
                break;
            case 5:
                championName = "Xin Zhao"; //
                break;
            case 29:
                championName = "Twitch"; // //143jung
                break;
            case 11:
                championName = "Master Yi"; //
                break;
            case 44:
                championName = "Taric"; //
                break;
            case 32:
                championName = "Amumu"; //
                break;
            case 41:
                championName = "Gangplank"; //
                break;
            case 48:
                championName = "Trundle"; //137top //162jung //182supp
                break;
            case 38:
                championName = "Kassadin"; //
                break;
            case 161:
                championName = "Vel'Koz"; //102mid //144supp
                break;
            case 143:
                championName = "Zyra"; //84supp
                break;
            case 267:
                championName = "Nami"; //
                break;
            case 59:
                championName = "Jarvan IV"; //
                break;
            case 81:
                championName = "Ezreal"; //
                break;
            case 516:
                championName = "Ornn"; //104top
                break;
            case 141:
                championName = "Kayn"; //
                break;
            case 497:
                championName = "Rakan"; //
                break;
            case 142:
                championName = "Zoe"; // //177supp
                break;
            case 498:
                championName = "Xayah"; //
                break;
            case 164:
                championName = "Camille"; // //98jung
                break;
            default:
                break;
        }
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

        if (wantedRating == 0) { //WinRate Checked
            float tempRate;
            String tempRateString;
            String splitRateString;
            int m = mWinRates.size();
            String tempRank;

            if (m != 0)
            {
                mImageUrls.clear();
                mNames.clear();
                mWinRates.clear();
                mChampionPosition.clear();
                mRankPosition.clear();
//                for (int i = 0; i < m; i++)
//                {
//                    mImageUrls.remove(i);
//                    mNames.remove(i);
//                    mWinRates.remove(i);
//                    mChampionPosition.remove(i);
//                    mRankPosition.remove(i);
//                    m--;
//                    i--;
//                }
            }

            for (int i = 0; i < rates.size()-1; i++)
            {
                tempRank = Integer.toString(i + 1);
                tempRate = Float.parseFloat(rates.get(i).getWinRate()) * 100;
                tempRateString = Float.toString(tempRate) + "000";
                splitRateString = tempRateString.substring(0, 5);

                String tempPosition = rates.get(i).getChampionRole().replaceAll("DUO_CARRY", "ADC").replaceAll("DUO_SUPPORT", "SUPPORT");
                String tempImage = "https://www.mobafire.com/images/champion/icon/" + rates.get(i).getChampionId().toLowerCase()
                        .replaceAll("\\s+","-")
                        .replaceAll("'", "")
                        .replaceAll("\\.", "") + ".png";
                mImageUrls.add(tempImage);
                mNames.add(rates.get(i).getChampionId());
                mWinRates.add(splitRateString);
                mChampionPosition.add(tempPosition);
                mRankPosition.add(tempRank);
            }
        }

        if (wantedRating == 1) { //PickRate Checked
            float tempRate;
            String tempRateString;
            String splitRateString;
            int m = mWinRates.size();
            String tempRank;

            if (m != 0)
            {
                mImageUrls.clear();
                mNames.clear();
                mWinRates.clear();
                mChampionPosition.clear();
                mRankPosition.clear();
//                for (int i = 0; i < m; i++)
//                {
//                    mImageUrls.remove(i);
//                    mNames.remove(i);
//                    mWinRates.remove(i);
//                    mChampionPosition.remove(i);
//                    mRankPosition.remove(i);
//                    m--;
//                    i--;
//                }
            }

            for (int i = 0; i < rates.size()-1; i++)
            {
                tempRank = Integer.toString(i + 1);
                tempRate = Float.parseFloat(rates.get(i).getPlayRate()) * 100;
                tempRateString = Float.toString(tempRate) + "000";
                splitRateString = tempRateString.substring(0, 5);

                String tempPosition = rates.get(i).getChampionRole().replaceAll("DUO_CARRY", "ADC").replaceAll("DUO_SUPPORT", "SUPPORT");
                String tempImage = "https://www.mobafire.com/images/champion/icon/" + rates.get(i).getChampionId().toLowerCase()
                        .replaceAll("\\s+","-")
                        .replaceAll("'", "")
                        .replaceAll("\\.", "") + ".png";
                mImageUrls.add(tempImage);
                mNames.add(rates.get(i).getChampionId());
                mWinRates.add(splitRateString);
                mChampionPosition.add(tempPosition);
                mRankPosition.add(tempRank);
            }
        }

        if (wantedRating == 2) { //BanRate checked
            float tempRate;
            String tempRateString;
            String splitRateString;
            int m = mWinRates.size();
            String tempRank;

            if (m != 0)
            {
                mImageUrls.clear();
                mNames.clear();
                mWinRates.clear();
                mChampionPosition.clear();
                mRankPosition.clear();
//                for (int i = 0; i < m; i++)
//                {
//                    mImageUrls.remove(i);
//                    mNames.remove(i);
//                    mWinRates.remove(i);
//                    mChampionPosition.remove(i);
//                    mRankPosition.remove(i);
//                    m--;
//                    i--;
//                }
            }

            for (int i = 0; i < rates.size()-1; i++)
            {
                tempRank = Integer.toString(i + 1);
                tempRate = Float.parseFloat(rates.get(i).getBanRate()) * 100;
                tempRateString = Float.toString(tempRate) + "000";
                splitRateString = tempRateString.substring(0, 5);

                String tempPosition = rates.get(i).getChampionRole().replaceAll("DUO_CARRY", "ADC").replaceAll("DUO_SUPPORT", "SUPPORT");
                String tempImage = "https://www.mobafire.com/images/champion/icon/" + rates.get(i).getChampionId().toLowerCase()
                        .replaceAll("\\s+","-")
                        .replaceAll("'", "")
                        .replaceAll("\\.", "") + ".png";
                mImageUrls.add(tempImage);
                mNames.add(rates.get(i).getChampionId());
                mWinRates.add(splitRateString);
                mChampionPosition.add(tempPosition);
                mRankPosition.add(tempRank);
            }
        }


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
                        mRankPosition.remove(i);
                        m--;
                        i--;
                    }
                }

                //Sort Top Champion RankPositions
                for (int i = 0; i < mWinRates.size(); i++)
                {
                    if (mChampionPosition.get(i).toLowerCase().contains("sup") && mNames.get(i).toLowerCase().contains("kennen"))
                    {
                        mImageUrls.remove(i);
                        mNames.remove(i);
                        mWinRates.remove(i);
                        mChampionPosition.remove(i);
                        mRankPosition.remove(i);
                    }

                    mRankPosition.set(i, Integer.toString(i+1));
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
                        mRankPosition.remove(i);
                        m--;
                        i--;
                    }
                }

                //Sort Jungle Champion RankPositions
                for (int i = 0; i < mWinRates.size(); i++)
                {
                    if (mChampionPosition.get(i).toLowerCase().contains("sup") && mNames.get(i).toLowerCase().contains("kennen"))
                    {
                        mImageUrls.remove(i);
                        mNames.remove(i);
                        mWinRates.remove(i);
                        mChampionPosition.remove(i);
                        mRankPosition.remove(i);
                    }
                    mRankPosition.set(i, Integer.toString(i+1));
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
                        mRankPosition.remove(i);
                        m--;
                        i--;
                    }
                }

                //Sort Mid Champion RankPositions
                for (int i = 0; i < mWinRates.size(); i++)
                {
                    if (mChampionPosition.get(i).toLowerCase().contains("sup") && mNames.get(i).toLowerCase().contains("kennen"))
                    {
                        mImageUrls.remove(i);
                        mNames.remove(i);
                        mWinRates.remove(i);
                        mChampionPosition.remove(i);
                        mRankPosition.remove(i);
                    }
                    mRankPosition.set(i, Integer.toString(i+1));
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
                        mRankPosition.remove(i);
                        m--;
                        i--;
                    }
                }
                //Sort Support Champion RankPositions
                for (int i = 0; i < mWinRates.size(); i++)
                {
                    mRankPosition.set(i, Integer.toString(i+1));
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
                        mRankPosition.remove(i);
                        m--;
                        i--;
                        Log.v(TAG, String.valueOf(mWinRates.size()));
                    }
                }

                //Sort ADC Champion RankPositions
                for (int i = 0; i < mWinRates.size(); i++)
                {
                    if (mChampionPosition.get(i).toLowerCase().contains("sup") && mNames.get(i).toLowerCase().contains("kennen"))
                    {
                        mImageUrls.remove(i);
                        mNames.remove(i);
                        mWinRates.remove(i);
                        mChampionPosition.remove(i);
                        mRankPosition.remove(i);
                    }

                    mRankPosition.set(i, Integer.toString(i+1));
                }

                break;

            default:
                break;

        }
//

        //FINISH LATER, SORT FOR ALL RANK POSITIONS OF DIFFERENT LANES

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



        for (int i = n-1; i > 0; i--)
        {

            for (int j = n-i-1; j < 0; j--)
            {
                if (Integer.valueOf(mRankPosition.get(j)) < Integer.valueOf(mRankPosition.get(j + 1))) {
                    int rep = j+1;

                    String tempRankPosition = mRankPosition.get(j);
                    mRankPosition.set(j, mRankPosition.get(rep));
                    mRankPosition.set(rep, tempRankPosition);
                }
            }
        }

        initChampList(rView);
    }

    private void initChampList(RecyclerView rView){
        Log.d(TAG, "initChampList: initialized RecyclerView");
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(getContext(), mNames, mImageUrls, mWinRates, mChampionPosition, mRankPosition);
        rView.setAdapter(adapter);
        rView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public static Fragment newInstance() {
        return new MainChampions();
    }
}