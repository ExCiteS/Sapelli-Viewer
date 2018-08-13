package uk.ac.excites.ucl.sapelliviewer.ui;

public interface DocumentFragmentListener {

    void OnFragmentAttached(String type, int id);

    void OnFragmentDetached(String type, int id);
}

