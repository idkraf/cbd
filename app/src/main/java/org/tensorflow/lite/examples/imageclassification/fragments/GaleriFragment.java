package org.tensorflow.lite.examples.imageclassification.fragments;

import static androidx.core.view.ViewCompat.setTransitionName;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.tensorflow.lite.examples.imageclassification.R;
import org.tensorflow.lite.examples.imageclassification.databinding.FragmentGaleriBinding;
import org.tensorflow.lite.examples.imageclassification.utils.imageIndicatorListener;
import org.tensorflow.lite.examples.imageclassification.utils.pictureFacer;
import org.tensorflow.lite.examples.imageclassification.utils.recyclerViewPagerImageIndicator;

import java.util.ArrayList;

public class GaleriFragment extends Fragment implements imageIndicatorListener {
    private FragmentGaleriBinding binding;

    private  ArrayList<pictureFacer> allImages = new ArrayList<>();
    private int position;
    private Context animeContx;
    private ImageView image;
    private int viewVisibilityController;
    private int viewVisibilitylooper;
    private ImagesPagerAdapter pagingImages;
    private int previousSelected = -1;


    public GaleriFragment(){

    }

    public GaleriFragment(ArrayList<pictureFacer> allImages, int imagePosition, Context anim) {
        this.allImages = allImages;
        this.position = imagePosition;
        this.animeContx = anim;
    }

    public static GaleriFragment newInstance(ArrayList<pictureFacer> allImages, int imagePosition, Context anim) {
        GaleriFragment fragment = new GaleriFragment(allImages,imagePosition,anim);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGaleriBinding
                .inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /**
         * initialisation of the recyclerView visibility control integers
         */
        viewVisibilityController = 0;
        viewVisibilitylooper = 0;

        pagingImages = new ImagesPagerAdapter();
        binding.imagePager.setAdapter(pagingImages);
        binding.imagePager.setOffscreenPageLimit(3);
        binding.imagePager.setCurrentItem(position);

        binding.indicatorRecycler.hasFixedSize();
        binding.indicatorRecycler.setLayoutManager(new GridLayoutManager(getContext(),1,RecyclerView.HORIZONTAL,false));
        RecyclerView.Adapter indicatorAdapter = new recyclerViewPagerImageIndicator(allImages,getContext(),this);
        binding.indicatorRecycler.setAdapter(indicatorAdapter);


        //adjusting the recyclerView indicator to the current position of the viewPager, also highlights the image in recyclerView with respect to the
        //viewPager's position
        allImages.get(position).setSelected(true);
        previousSelected = position;
        indicatorAdapter.notifyDataSetChanged();
        binding.indicatorRecycler.scrollToPosition(position);

        /**
         * this listener controls the visibility of the recyclerView
         * indication and it current position in respect to the image ViewPager
         */
        binding.imagePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if(previousSelected != -1){
                    allImages.get(previousSelected).setSelected(false);
                    previousSelected = position;
                    allImages.get(position).setSelected(true);
                    binding.indicatorRecycler.getAdapter().notifyDataSetChanged();
                    binding.indicatorRecycler.scrollToPosition(position);
                }else{
                    previousSelected = position;
                    allImages.get(position).setSelected(true);
                    binding.indicatorRecycler.getAdapter().notifyDataSetChanged();
                    binding.indicatorRecycler.scrollToPosition(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        binding.indicatorRecycler.setOnTouchListener((v, event) -> {
            /**
             *  uncomment the below condition to control recyclerView visibility automatically
             *  when image is clicked also uncomment the condition set on the image's onClickListener in the ImagesPagerAdapter adapter
             */
            /*if(viewVisibilityController == 0){
                indicatorRecycler.setVisibility(View.VISIBLE);
                visibiling();
            }else{
                viewVisibilitylooper++;
            }*/
            return false;
        });
    }

    @Override
    public void onImageIndicatorClicked(int ImagePosition) {

        //the below lines of code highlights the currently select image in  the indicatorRecycler with respect to the viewPager position
        if(previousSelected != -1){
            allImages.get(previousSelected).setSelected(false);
            previousSelected = ImagePosition;
            binding.indicatorRecycler.getAdapter().notifyDataSetChanged();
        }else{
            previousSelected = ImagePosition;
        }

        binding.imagePager.setCurrentItem(ImagePosition);
    }

    /**
     * the imageViewPager's adapter
     */
    private class ImagesPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return allImages.size();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup containerCollection, int position) {
            LayoutInflater layoutinflater = (LayoutInflater) containerCollection.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutinflater.inflate(R.layout.picture_browser_pager,null);
            image = view.findViewById(R.id.image);

            setTransitionName(image, String.valueOf(position)+"picture");

            pictureFacer pic = allImages.get(position);
            Glide.with(animeContx)
                    .load(pic.getPicturePath())
                    .apply(new RequestOptions().fitCenter())
                    .into(image);

            image.setOnClickListener(v -> {

                if(binding.indicatorRecycler.getVisibility() == View.GONE){
                    binding.indicatorRecycler.setVisibility(View.VISIBLE);
                }else{
                    binding.indicatorRecycler.setVisibility(View.GONE);
                }

                /**
                 * uncomment the below condition and comment the one above to control recyclerView visibility automatically
                 * when image is clicked
                 */
                /*if(viewVisibilityController == 0){
                 indicatorRecycler.setVisibility(View.VISIBLE);
                 visibiling();
             }else{
                 viewVisibilitylooper++;
             }*/

            });



            ((ViewPager) containerCollection).addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup containerCollection, int position, Object view) {
            ((ViewPager) containerCollection).removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == ((View) object);
        }
    }

    /**
     * function for controlling the visibility of the recyclerView indicator
     */
    private void visibiling(){
        viewVisibilityController = 1;
        final int checker = viewVisibilitylooper;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(viewVisibilitylooper > checker){
                    visibiling();
                }else{
                    binding.indicatorRecycler.setVisibility(View.GONE);
                    viewVisibilityController = 0;

                    viewVisibilitylooper = 0;
                }
            }
        }, 4000);
    }
}
