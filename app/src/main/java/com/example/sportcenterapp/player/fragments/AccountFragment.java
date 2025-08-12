package com.example.sportcenterapp.player.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.example.sportcenterapp.R;

public class AccountFragment extends Fragment {
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater i, @Nullable ViewGroup c, @Nullable Bundle b) {
        View v = i.inflate(R.layout.fragment_account, c, false);
        Button btnLogout = v.findViewById(R.id.btnLogoutPlayer);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(vw -> Toast.makeText(requireContext(), "Logout: sẽ làm ở bước sau", Toast.LENGTH_SHORT).show());
        }
        return v;
    }
}
