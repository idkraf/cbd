package org.tensorflow.lite.examples.imageclassification.fragments;

import static androidx.core.view.ViewCompat.setTransitionName;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import org.tensorflow.lite.examples.imageclassification.R;
import org.tensorflow.lite.examples.imageclassification.databinding.FragmentGaleriBinding;
import org.tensorflow.lite.examples.imageclassification.utils.BackgroundRemover;
import org.tensorflow.lite.examples.imageclassification.utils.OnBackgroundChangeListener;
import org.tensorflow.lite.examples.imageclassification.utils.imageIndicatorListener;
import org.tensorflow.lite.examples.imageclassification.utils.pictureFacer;
import org.tensorflow.lite.examples.imageclassification.utils.recyclerViewPagerImageIndicator;


import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.tensorflow.lite.examples.imageclassification.ml.Model;

public class GaleriFragment extends Fragment implements imageIndicatorListener{
    private FragmentGaleriBinding binding;

    private  ArrayList<pictureFacer> allImages = new ArrayList<>();
    private int position;
    private Context animeContx;
    private int viewVisibilityController;
    private int viewVisibilitylooper;
    private ImagesPagerAdapter pagingImages;
    private int previousSelected = -1;

    private ImageView image;
    int imageSize = 224;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGaleriBinding
                .inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ((AppCompatActivity)getActivity()).onBackPressed();
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }



    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)getActivity()).setSupportActionBar(binding.toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

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

    @SuppressLint("NotifyDataSetChanged")
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
            //recyclerviewResults = view.findViewById(R.id.recyclerview_resultss);
            Button btnRemove = view.findViewById(R.id.buttonRemove);
            Button btnDeteksi = view.findViewById(R.id.buttonDeteksi);
            setTransitionName(image, String.valueOf(position)+"picture");

            pictureFacer pic = allImages.get(position);
            //Glide.with(animeContx)
            //        .load(pic.getPicturePath())
            //        .apply(new RequestOptions().fitCenter())
            //        .into(image);
            //image.setImageURI(Uri.parse(pic.getImageUri()));
            image.setImageURI(Uri.fromFile(new File(pic.getPicturePath())));
           // image.setOnClickListener(v -> {
           //     if(binding.indicatorRecycler.getVisibility() == View.GONE){
           //         binding.indicatorRecycler.setVisibility(View.VISIBLE);
           //         binding.recyclerviewResultss.setVisibility(View.GONE);
           //         binding.bottomSheetLayout.bottomSheetLayout.setVisibility(View.GONE);

           //     }else{
           //         binding.indicatorRecycler.setVisibility(View.GONE);
           //         binding.recyclerviewResultss.setVisibility(View.VISIBLE);
           //         binding.bottomSheetLayout.bottomSheetLayout.setVisibility(View.VISIBLE);
           //     }
           // });



            btnRemove.setOnClickListener(v->{
                //image.invalidate();
                //image.buildDrawingCache();
                //Bitmap bmap = image.getDrawingCache();
                Uri dat = Uri.fromFile(new File(pic.getPicturePath()));
                Bitmap bmap = null;

                try {
                    bmap = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getContext()).getContentResolver(), dat);
                    BackgroundRemover.INSTANCE.bitmapForProcessing(
                            bmap,
                            true,
                            new OnBackgroundChangeListener() {
                                @Override
                                public void onSuccess(@NonNull Bitmap bitmap) {
                                    image.setImageBitmap(bitmap);
                                }

                                @Override
                                public void onFailed(@NonNull Exception exception) {

                                    Toast.makeText(requireContext(), "Error Occur", Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //int dimension = Math.min(bmap.getWidth(), bmap.getHeight());

            });

            btnDeteksi.setOnClickListener(v->{
                //image.invalidate();
                image.buildDrawingCache();
                Bitmap bitmap = image.getDrawingCache();
               //if (bitmapBuffer == null) {
                 //   bitmapBuffer = Bitmap.createBitmap(
                //           image.getWidth(),
                 //           image.getHeight(),
                //            Bitmap.Config.ARGB_8888);
                    classifyImage(bitmap);
              //  }
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


    public  void classifyImage(Bitmap image){
        try {
            Model model = Model.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for(int i = 0; i < imageSize; i ++){
                for(int j = 0; j < imageSize; j++){
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 1));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 1));
                }
            }
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            // find the index of the class with the biggest confidence.
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            String[] classes = {"DarkRoasting", "GreenRoasting", "LightRoasting","MediumRoasting"};
            binding.result.setText(classes[maxPos]);

            String s = "";
            for(int i = 0; i < classes.length; i++ ){
                s += String.format("%s : %.1f%%\n", classes[i], confidences[i] * 100);
            }
            binding.confidence.setText(s);
            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

}
