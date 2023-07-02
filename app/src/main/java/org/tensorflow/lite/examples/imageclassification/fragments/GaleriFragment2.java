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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageAnalysis;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.tensorflow.lite.examples.imageclassification.ImageClassifierHelper;
import org.tensorflow.lite.examples.imageclassification.R;
import org.tensorflow.lite.examples.imageclassification.databinding.FragmentGaleri2Binding;
import org.tensorflow.lite.examples.imageclassification.databinding.FragmentGaleriBinding;
import org.tensorflow.lite.examples.imageclassification.utils.BackgroundRemover;
import org.tensorflow.lite.examples.imageclassification.utils.OnBackgroundChangeListener;
import org.tensorflow.lite.examples.imageclassification.utils.imageIndicatorListener;
import org.tensorflow.lite.examples.imageclassification.utils.pictureFacer;
import org.tensorflow.lite.examples.imageclassification.utils.recyclerViewPagerImageIndicator;
import org.tensorflow.lite.task.vision.classifier.Classifications;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class GaleriFragment2 extends Fragment implements imageIndicatorListener, ImageClassifierHelper.ClassifierListener{
    private FragmentGaleri2Binding binding;

    private  ArrayList<pictureFacer> allImages = new ArrayList<>();
    private int position;
    private Context animeContx;
    private int viewVisibilityController;
    private int viewVisibilitylooper;
    private ImagesPagerAdapter pagingImages;
    private int previousSelected = -1;

    private ImageView image;
    private RecyclerView recyclerviewResults;
    private Bitmap bitmapBuffer;
    private ImageClassifierHelper imageClassifierHelper;
    private ClassificationResultAdapter classificationResultsAdapter;
    private ImageAnalysis imageAnalyzer;
    private final Object task = new Object();

    public GaleriFragment2(){

    }

    public GaleriFragment2(ArrayList<pictureFacer> allImages, int imagePosition, Context anim) {
        this.allImages = allImages;
        this.position = imagePosition;
        this.animeContx = anim;
    }

    public static GaleriFragment2 newInstance(ArrayList<pictureFacer> allImages, int imagePosition, Context anim) {
        GaleriFragment2 fragment = new GaleriFragment2(allImages,imagePosition,anim);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGaleri2Binding
                .inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        synchronized (task) {
            imageClassifierHelper.clearImageClassifier();
        }
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

    @Override
    public void onError(String error) {

        requireActivity().runOnUiThread(() -> {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            classificationResultsAdapter.updateResults(new ArrayList<>());
        });
    }

    @Override
    public void onResults(List<Classifications> results, long inferenceTime) {
        requireActivity().runOnUiThread(() -> {
            classificationResultsAdapter.updateResults(results.get(0).getCategories());
            binding.bottomSheetLayout.inferenceTimeVal
                    .setText(String.format(Locale.US, "%d ms", inferenceTime));
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity)getActivity()).setSupportActionBar(binding.toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        imageClassifierHelper = ImageClassifierHelper.create(requireContext()
                , this);

        // setup result adapter
        classificationResultsAdapter = new ClassificationResultAdapter();
        classificationResultsAdapter
                .updateAdapterSize(imageClassifierHelper.getMaxResults());
        binding.recyclerviewResultss
                .setAdapter(classificationResultsAdapter);
        binding.recyclerviewResultss
                .setLayoutManager(new LinearLayoutManager(requireContext()));
        initBottomSheetControls();
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
                synchronized (task) {
                    imageClassifierHelper.clearImageClassifier();
                    classificationResultsAdapter.updateResults(new ArrayList<>());
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
        synchronized (task) {
            imageClassifierHelper.clearImageClassifier();
            classificationResultsAdapter.updateResults(new ArrayList<>());
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
                if (bitmapBuffer == null) {
                    bitmapBuffer = Bitmap.createBitmap(
                            image.getWidth(),
                            image.getHeight(),
                            Bitmap.Config.ARGB_8888);
                    classifyImage(bitmap);
                }
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


    public void classifyImage(Bitmap image){
        // Copy out RGB bits to the shared bitmap buffer
        //bitmapBuffer.copyPixelsFromBuffer(image.getPlanes()[0].getBuffer());
        int imageRotation = 0;
        //image.close();
        synchronized (task) {
            // Pass Bitmap and rotation to the image classifier helper for
            // processing and classification
            imageClassifierHelper.classify(bitmapBuffer, imageRotation);
        }
    }

    // Update the values displayed in the bottom sheet. Reset classifier.
    private void updateControlsUi() {
        binding.bottomSheetLayout.maxResultsValue
                .setText(String.valueOf(imageClassifierHelper.getMaxResults()));
        binding.bottomSheetLayout.thresholdValue
                .setText(String.format(Locale.US, "%.2f",
                        imageClassifierHelper.getThreshold()));
        binding.bottomSheetLayout.threadsValue
                .setText(String.valueOf(imageClassifierHelper.getNumThreads()));
        // Needs to be cleared instead of reinitialized because the GPU
        // delegate needs to be initialized on the thread using it when
        // applicable
        synchronized (task) {
            imageClassifierHelper.clearImageClassifier();
        }
    }

    private void initBottomSheetControls() {
        // When clicked, lower classification score threshold floor
        binding.bottomSheetLayout.thresholdMinus
                .setOnClickListener(view -> {
                    float threshold = imageClassifierHelper.getThreshold();
                    if (threshold >= 0.1) {
                        imageClassifierHelper.setThreshold(threshold - 0.1f);
                        updateControlsUi();
                    }
                });

        // When clicked, raise classification score threshold floor
        binding.bottomSheetLayout.thresholdPlus
                .setOnClickListener(view -> {
                    float threshold = imageClassifierHelper.getThreshold();
                    if (threshold < 0.9) {
                        imageClassifierHelper.setThreshold(threshold + 0.1f);
                        updateControlsUi();
                    }
                });

        // When clicked, reduce the number of objects that can be classified
        // at a time
        binding.bottomSheetLayout.maxResultsMinus
                .setOnClickListener(view -> {
                    int maxResults = imageClassifierHelper.getMaxResults();
                    if (maxResults > 1) {
                        imageClassifierHelper.setMaxResults(maxResults - 1);
                        updateControlsUi();
                        classificationResultsAdapter.updateAdapterSize(
                                imageClassifierHelper.getMaxResults()
                        );
                    }
                });

        // When clicked, increase the number of objects that can be
        // classified at a time
        binding.bottomSheetLayout.maxResultsPlus
                .setOnClickListener(view -> {
                    int maxResults = imageClassifierHelper.getMaxResults();
                    if (maxResults < 3) {
                        imageClassifierHelper.setMaxResults(maxResults + 1);
                        updateControlsUi();
                        classificationResultsAdapter.updateAdapterSize(
                                imageClassifierHelper.getMaxResults()
                        );
                    }
                });

        // When clicked, decrease the number of threads used for classification
        binding.bottomSheetLayout.threadsMinus
                .setOnClickListener(view -> {
                    int numThreads = imageClassifierHelper.getNumThreads();
                    if (numThreads > 1) {
                        imageClassifierHelper.setNumThreads(numThreads - 1);
                        updateControlsUi();
                    }
                });

        // When clicked, increase the number of threads used for classification
        binding.bottomSheetLayout.threadsPlus
                .setOnClickListener(view -> {
                    int numThreads = imageClassifierHelper.getNumThreads();
                    if (numThreads < 4) {
                        imageClassifierHelper.setNumThreads(numThreads + 1);
                        updateControlsUi();
                    }
                });

        // When clicked, change the underlying hardware used for inference.
        // Current options are CPU,GPU, and NNAPI
        binding.bottomSheetLayout.spinnerDelegate
                .setSelection(0, false);
        binding.bottomSheetLayout.spinnerDelegate
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView,
                                               View view,
                                               int position,
                                               long id) {
                        imageClassifierHelper.setCurrentDelegate(position);
                        updateControlsUi();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        // no-op
                    }
                });

        // When clicked, change the underlying model used for object
        // classification
        binding.bottomSheetLayout.spinnerModel
                .setSelection(0, false);
        binding.bottomSheetLayout.spinnerModel
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView,
                                               View view,
                                               int position,
                                               long id) {
                        imageClassifierHelper.setCurrentModel(position);
                        updateControlsUi();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        // no-op
                    }
                });
    }


}
