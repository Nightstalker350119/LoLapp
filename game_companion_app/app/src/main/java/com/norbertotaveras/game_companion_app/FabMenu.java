package com.norbertotaveras.game_companion_app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Norberto Taveras on 3/2/2018.
 */

public class FabMenu {
    private final View view;
    private final View container;
    private FabButton activateButton;
    private FabButton[] items;
    private boolean isOpen;
    private FabAnimations animations;
    private FabButton selectedButton;
    private int selectedColor;
    private int deselectedColor;

    public FabMenu(View view, int container_id) {
        this.view = view;
        this.container = view.findViewById(container_id);
        animations = new FabAnimations();

        selectedColor = ContextCompat.getColor(view.getContext(), R.color.colorPrimary);
        deselectedColor = ContextCompat.getColor(view.getContext(), R.color.textColorDim);
    }

    public void setActivateButton(FabButtonShowMenu activateButton) {
        this.activateButton = activateButton;
    }

    public void setButtons(FabButton[] items) {
        this.items = items;

        for (FabButton item : items)
            item.setMenu(this);
    }

    public void setItemsVisible(boolean visible) {
        if (isOpen == visible)
            return;

        isOpen = visible;

        activateButton.button.startAnimation(visible
                ? animations.clockwise : animations.counterclockwise);

        for (FabButton item : items)
            item.setVisible(visible);
    }

    private void setMenuVisible(boolean visible) {
        if (!visible)
            setItemsVisible(visible);

        container.setVisibility(visible ? View.VISIBLE : View.GONE);
        //activateButton.setVisible(visible);
    }

    public void setSelectedButton(FabButton selectedButton) {
        this.selectedButton = selectedButton;
        for (FabButton button : items)
            button.setSelected(button == selectedButton);
    }

    public void setSelectedIndex(int index) {
        setSelectedButton(items[index]);
    }

    public FabButton getSelectedButton() {
        return selectedButton;
    }

    public void toggle() {
        setItemsVisible(!isOpen);
    }

    public boolean isVisible() {
        return isOpen;
    }

    public static abstract class FabButton
            implements View.OnClickListener, Animation.AnimationListener {
        protected final View button;
        protected Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        protected FabMenu owner;

        public FabButton(FabMenu owner, int id) {
            this.owner = owner;

            button = owner.view.findViewById(id);

            button.setOnClickListener(this);

            // Add click handler to all of the children
            if (button instanceof ViewGroup) {
                ViewGroup buttonGroup = (ViewGroup) button;
                for (int i = 0, e = buttonGroup.getChildCount(); i < e; ++i)
                    buttonGroup.getChildAt(i).setOnClickListener(this);
            }

            fadeOut.setDuration(3000);
            fadeOut.setAnimationListener(this);
        }

        public void setMenu(FabMenu owner) {
            this.owner = owner;
        }

        public void setVisible(boolean visible) {
            if (button instanceof FloatingActionButton) {
                FloatingActionButton fab = (FloatingActionButton)button;
                Log.v("FabMenuButton", "Setting FAB visibility to " + visible +
                        " on " + fab.toString());
                if (visible)
                    fab.show();
                else
                    fab.hide();
            } else {
                Log.v("FabMenuButton", "Setting button visibility to " + visible +
                        " on " + button.toString());
                button.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            }
            //button.startAnimation(fadeOut);

            button.startAnimation(visible ? owner.animations.open : owner.animations.close);
        }

        // Return false to hide the menu on click
        public abstract boolean onClick();

        @Override
        public void onClick(View view) {
            if (!onClick()) {
                owner.setSelectedButton(this);
                owner.setItemsVisible(false);
            }
        }

        @Override
        public void onAnimationStart(Animation animation) {
            if (animation == owner.animations.open) {
                button.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (animation == fadeOut) {
                button.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        public void setSelected(boolean isSelected) {
            FloatingActionButton fab = null;

            if (button instanceof FloatingActionButton) {
                fab = (FloatingActionButton)button;
            } else {
                ViewGroup group = (ViewGroup)button;
                for (int i = 0, e = group.getChildCount(); i < e; ++i) {
                    View child = group.getChildAt(i);
                    if (child instanceof FloatingActionButton) {
                        fab = (FloatingActionButton)child;
                        break;
                    }
                }
            }

            if (fab != null) {
                ColorStateList colorList = ColorStateList.valueOf(
                        isSelected ? owner.selectedColor : owner.deselectedColor);
                fab.setBackgroundTintList(colorList);
            }
        }
    }

    public static class FabButtonShowMenu extends FabButton {
        public FabButtonShowMenu(FabMenu owner, int id) {
            super(owner, id);
        }

        @Override
        public boolean onClick() {
            owner.toggle();
            return true;
        }

        @Override
        public void setSelected(boolean isSelected) {
            // Ignore
        }
    }

    public static class TabSwitcher extends ViewPager.SimpleOnPageChangeListener {
        // List of FabMenu objects in the same order as ViewPager index
        List<FabMenu> tabMenus;
        int currentTab;

        public TabSwitcher() {
            tabMenus = new ArrayList<>();
        }

        public void setViewPager(ViewPager viewPager) {
            viewPager.addOnPageChangeListener(this);
            currentTab = -1;
            onPageSelected(viewPager.getCurrentItem());
        }

        public void addMenuToTab(FabMenu menu) {
            tabMenus.add(menu);
        }

        public boolean onBackPressed() {
            for (int i = 0, e = tabMenus.size(); i < e; ++i) {
                FabMenu menu = tabMenus.get(i);
                if (menu.isVisible()) {
                    menu.setItemsVisible(false);
                    return true;
                }
            }
            return false;
        }


        @Override
        public void onPageSelected(int position) {
            if (currentTab == position)
                return;

            Log.v("FabMenuTabSwitcher", "Switching to tab " + position);
            currentTab = position;

            for (int i = 0, e = tabMenus.size(); i < e; ++i)
                tabMenus.get(i).setMenuVisible(i == position);
        }
    }

    private class FabAnimations {
        private final Animation open;
        private final Animation close;
        private final Animation clockwise;
        private final Animation counterclockwise;

        public FabAnimations() {
            final Context ctx = view.getContext();
            open = AnimationUtils.loadAnimation(ctx, R.anim.fab_open);
            close = AnimationUtils.loadAnimation(ctx, R.anim.fab_close);
            clockwise = AnimationUtils.loadAnimation(ctx, R.anim.rotate_clockwise);
            counterclockwise = AnimationUtils.loadAnimation(ctx, R.anim.rotate_counterclockwise);
        }
    }
}
