package uk.ac.excites.ucl.sapelliviewer.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import uk.ac.excites.ucl.sapelliviewer.R;
import uk.ac.excites.ucl.sapelliviewer.db.AppDatabase;


public class DetailsFragment extends Fragment {
    private static final String CONTRIBUTION_ID = "contributionID";

    private int contributionId;
    private OnFragmentInteractionListener interactionListener;

    public DetailsFragment() {
        // Required empty public constructor
    }

    public static DetailsFragment newInstance(int contributionId) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putInt(CONTRIBUTION_ID, contributionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            contributionId = getArguments().getInt(CONTRIBUTION_ID);

        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        TextView txtContributionId = view.findViewById(R.id.contrib_id_text);
        txtContributionId.setText(String.valueOf(contributionId));
        Button btnClose = view.findViewById(R.id.btn_close_fragment);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeView();
            }
        });

        RecyclerView valueRecyclerView = view.findViewById(R.id.value_recycler_view);
        valueRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        AppDatabase.getAppDatabase(getActivity()).contributionDao().getPropertiesByContribution(contributionId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(contributionProperties -> valueRecyclerView.setAdapter(new ContributionValueAdapter(getActivity(), contributionProperties)));
        RecyclerView photoRecyclerView = view.findViewById(R.id.photo_recycler_view);
        photoRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        AppDatabase.getAppDatabase(getActivity()).contributionDao().getPhotosByContribution(contributionId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(photos -> photoRecyclerView.setAdapter(new ContributionPhotoAdapter(getActivity(), photos)));

        return view;
    }

    public void closeView() {
        if (interactionListener != null) {
            interactionListener.onFragmentInteraction();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            interactionListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        interactionListener = null;
    }

    public int getContributionId() {
        return contributionId;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction();
    }
}
