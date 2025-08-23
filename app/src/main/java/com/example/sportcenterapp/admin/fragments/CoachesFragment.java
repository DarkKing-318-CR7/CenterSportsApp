// com.example.sportcenterapp.admin.fragments.CoachesFragment
package com.example.sportcenterapp.admin.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.example.sportcenterapp.R;
import com.example.sportcenterapp.adapters.CoachSimpleAdapter;
import com.example.sportcenterapp.database.DatabaseHelper;
import com.example.sportcenterapp.models.Coach;
import java.util.ArrayList;
import java.util.List;

public class CoachesFragment extends Fragment {

    private DatabaseHelper db;
    private RecyclerView rv;
    private TextView empty;
    private Button btnAdd;
    private CoachSimpleAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_coaches, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);
        db = new DatabaseHelper(requireContext());

        rv     = v.findViewById(R.id.rvCoach);
        empty  = v.findViewById(R.id.emptyCoaches);
        btnAdd = v.findViewById(R.id.btnAddCoach);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CoachSimpleAdapter(new ArrayList<>(), this::showEditDialog);
        rv.setAdapter(adapter);

        // Swipe để xoá
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder t) { return false; }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getBindingAdapterPosition();
                Coach c = getItemAt(pos);
                if (c == null) { adapter.notifyItemChanged(pos); return; }
                new AlertDialog.Builder(requireContext())
                        .setTitle("Xoá HLV?")
                        .setMessage("Bạn có chắc muốn xoá " + c.getName() + "?")
                        .setNegativeButton("Huỷ", (d,w)-> adapter.notifyItemChanged(pos))
                        .setPositiveButton("Xoá", (d,w)-> {
                            if (db.deleteCoach(c.getId())) loadData();
                            else { Toast.makeText(getContext(),"Xoá thất bại",Toast.LENGTH_SHORT).show(); adapter.notifyItemChanged(pos); }
                        }).show();
            }
        }).attachToRecyclerView(rv);

        btnAdd.setOnClickListener(vw -> showEditDialog(null));

        loadData();
    }

    private Coach getItemAt(int pos){
        // lấy ra từ adapter một cách an toàn
        // (adapter không expose list nên tự nạp lại rồi lấy theo id nếu cần)
        List<Coach> list = db.getAllCoaches();
        if (pos >=0 && pos < list.size()) return list.get(pos);
        return null;
    }

    private void loadData() {
        List<Coach> data = db.getAllCoaches();
        adapter.submit(data);
        empty.setVisibility(data.isEmpty()? View.VISIBLE : View.GONE);
    }

    /** Thêm (c==null) hoặc sửa (c!=null) */
    private void showEditDialog(@Nullable Coach c) {
        View form = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_admin_coach, null, false);

        EditText etName   = form.findViewById(R.id.etName);
        EditText etSport  = form.findViewById(R.id.etSport);
        EditText etLevel  = form.findViewById(R.id.etLevel);
        EditText etRate   = form.findViewById(R.id.etRate);
        EditText etAvatar = form.findViewById(R.id.etAvatar);
        EditText etBio    = form.findViewById(R.id.etBio);
        EditText etPhone  = form.findViewById(R.id.etPhone);
        EditText etEmail  = form.findViewById(R.id.etEmail);
        EditText etZalo   = form.findViewById(R.id.etZalo);

        if (c != null) {
            etName.setText(c.getName());
            etSport.setText(c.getSport());
            etLevel.setText(c.getLevel());
            etRate.setText(c.getRatePerHour() > 0 ? String.valueOf(c.getRatePerHour()) : "");
            etAvatar.setText(c.getAvatar());
            etBio.setText(c.getBio());
            etPhone.setText(c.getPhone());
            etEmail.setText(c.getEmail());
            etZalo.setText(c.getZalo());
        }

        AlertDialog dlg = new AlertDialog.Builder(requireContext())
                .setTitle(c==null ? "Thêm huấn luyện viên" : "Sửa huấn luyện viên")
                .setView(form)
                .setNegativeButton("Huỷ", null)
                .setPositiveButton(c==null ? "Thêm" : "Lưu", null) // set sau để tự validate
                .create();

        dlg.setOnShowListener(d -> {
            dlg.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name  = etName.getText().toString().trim();
                String sport = etSport.getText().toString().trim();
                if (name.isEmpty()) { etName.setError("Nhập tên"); return; }
                if (sport.isEmpty()){ etSport.setError("Nhập môn"); return; }
                double rate = 0;
                try { rate = Double.parseDouble(etRate.getText().toString().trim()); } catch (Exception ignore){}

                Coach m = (c==null? new Coach() : c);
                m.setName(name);
                m.setSport(sport);
                m.setLevel(etLevel.getText().toString().trim());
                m.setRatePerHour(rate);
                m.setAvatar(etAvatar.getText().toString().trim());
                m.setBio(etBio.getText().toString().trim());
                m.setPhone(etPhone.getText().toString().trim());
                m.setEmail(etEmail.getText().toString().trim());
                m.setZalo(etZalo.getText().toString().trim());

                boolean ok = (c==null) ? db.insertCoach(m) > 0
                        : db.updateCoach(m);
                if (ok) {
                    loadData();
                    dlg.dismiss();
                } else {
                    Toast.makeText(getContext(), "Lưu thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        });
        dlg.show();
    }
}
