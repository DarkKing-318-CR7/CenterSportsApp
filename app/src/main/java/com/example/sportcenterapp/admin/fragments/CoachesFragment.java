package com.example.sportcenterapp.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sportcenterapp.R;
import com.example.sportcenterapp.adapters.CoachSimpleAdapter;
import com.example.sportcenterapp.models.Coach;
import com.example.sportcenterapp.net.ApiClient;
import com.example.sportcenterapp.net.ApiService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Tab Admin > Tài khoản > HLV (list + thêm/sửa/xoá dùng API) */
public class CoachesFragment extends Fragment {

    private Spinner spnSport;
    private RecyclerView rv;
    private FloatingActionButton fab;
    private CoachSimpleAdapter adapter;
    private final List<Coach> data = new ArrayList<>();
    private ApiService api;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_coaches_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        api = ApiClient.build().create(ApiService.class);

        spnSport = v.findViewById(R.id.spnSport);
        rv       = v.findViewById(R.id.rvCoaches);
        fab      = v.findViewById(R.id.fabAddCoach);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // Adapter: click = sửa, long click = menu sửa/xoá
        adapter = new CoachSimpleAdapter(
                new ArrayList<>(),
                coach -> openCoachDialog(coach),                      // onItemClick -> Edit
                (anchor, coach) -> showItemMenu(anchor, coach)        // onLongClick -> menu Edit/Delete
        );
        rv.setAdapter(adapter);

        // Spinner bộ môn
        ArrayAdapter<String> spnAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                new ArrayList<>()
        );
        spnSport.setAdapter(spnAdapter);

        loadSports(spnAdapter);

        fab.setOnClickListener(vw -> openCoachDialog(null)); // Thêm mới

        spnSport.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                String sport = (String) parent.getItemAtPosition(pos);
                loadCoaches("Tất cả".equalsIgnoreCase(sport) ? null : sport);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
    }

    private void loadSports(ArrayAdapter<String> spnAdapter){
        api.getCoachSports().enqueue(new Callback<List<String>>() {
            @Override public void onResponse(Call<List<String>> call, Response<List<String>> resp) {
                List<String> list = new ArrayList<>();
                list.add("Tất cả");
                if (resp.isSuccessful() && resp.body()!=null) list.addAll(resp.body());
                spnAdapter.clear(); spnAdapter.addAll(list); spnAdapter.notifyDataSetChanged();
                loadCoaches(null);
            }
            @Override public void onFailure(Call<List<String>> call, Throwable t) {
                List<String> list = new ArrayList<>();
                list.add("Tất cả");
                spnAdapter.clear(); spnAdapter.addAll(list); spnAdapter.notifyDataSetChanged();
                loadCoaches(null);
            }
        });
    }

    private void loadCoaches(@Nullable String sport) {
        api.getCoaches(sport).enqueue(new Callback<List<Coach>>() {
            @Override public void onResponse(Call<List<Coach>> call, Response<List<Coach>> resp) {
                if (!resp.isSuccessful() || resp.body()==null) {
                    Toast.makeText(getContext(),"Không tải được HLV", Toast.LENGTH_SHORT).show();
                    return;
                }
                data.clear(); data.addAll(resp.body());
                adapter.submit(data);
            }
            @Override public void onFailure(Call<List<Coach>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng: "+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Dialog thêm/sửa. Nếu coach==null -> thêm, ngược lại là sửa */
    private void openCoachDialog(@Nullable Coach coach) {
        View form = LayoutInflater.from(getContext()).inflate(R.layout.dialog_admin_coach, null, false);
        EditText edName  = form.findViewById(R.id.edName);
        EditText edSport = form.findViewById(R.id.edSport);
        EditText edLevel = form.findViewById(R.id.edLevel);
        EditText edRate  = form.findViewById(R.id.edRate);
        EditText edPhone = form.findViewById(R.id.edPhone);
        EditText edEmail = form.findViewById(R.id.edEmail);
        EditText edZalo  = form.findViewById(R.id.edZalo);
        EditText edAvatar= form.findViewById(R.id.edAvatar);
        EditText edBio   = form.findViewById(R.id.edBio);

        if (coach != null) {
            edName.setText(coach.getName());
            edSport.setText(coach.getSport());
            edLevel.setText(coach.getLevel());
            if (coach.getRatePerHour() > 0) edRate.setText(String.valueOf((long) coach.getRatePerHour()));
            edPhone.setText(coach.getPhone());
            edEmail.setText(coach.getEmail());
            edZalo.setText(coach.getZalo());
            edAvatar.setText(coach.getAvatar());
            edBio.setText(coach.getBio());
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(coach == null ? "Thêm HLV" : "Sửa HLV")
                .setView(form)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", (d, w) -> {
                    ApiService.CoachSaveReq req = new ApiService.CoachSaveReq();
                    req.id    = (coach == null ? null : coach.getId());
                    req.name  = edName.getText().toString().trim();
                    req.sport = edSport.getText().toString().trim();
                    req.level = edLevel.getText().toString().trim();
                    try { req.ratePerHour = Double.parseDouble(edRate.getText().toString().trim()); }
                    catch (Exception e) { req.ratePerHour = 0; }
                    req.phone  = edPhone.getText().toString().trim();
                    req.email  = edEmail.getText().toString().trim();
                    req.zalo   = edZalo.getText().toString().trim();
                    req.avatar = edAvatar.getText().toString().trim();
                    req.bio    = edBio.getText().toString().trim();

                    api.saveCoach(req).enqueue(new Callback<ApiService.SimpleRespId>() {
                        @Override public void onResponse(Call<ApiService.SimpleRespId> call, Response<ApiService.SimpleRespId> r) {
                            if (r.isSuccessful() && r.body()!=null && r.body().ok) {
                                Toast.makeText(getContext(), "Đã lưu HLV", Toast.LENGTH_SHORT).show();
                                String cur = (String) spnSport.getSelectedItem();
                                loadCoaches("Tất cả".equalsIgnoreCase(cur)? null : cur);
                            } else {
                                Toast.makeText(getContext(), "Lưu thất bại", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override public void onFailure(Call<ApiService.SimpleRespId> call, Throwable t) {
                            Toast.makeText(getContext(), "Lỗi mạng: "+t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }).show();
    }

    /** Menu nổi khi long-click item: Sửa / Xoá */
    private void showItemMenu(View anchor, Coach c) {
        PopupMenu pm = new PopupMenu(requireContext(), anchor);
        pm.getMenu().add(0, 1, 0, "Sửa");
        pm.getMenu().add(0, 2, 1, "Xoá");
        pm.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                openCoachDialog(c);
                return true;
            } else if (item.getItemId() == 2) {
                confirmDelete(c);
                return true;
            }
            return false;
        });
        pm.show();
    }

    private void confirmDelete(Coach c) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xoá HLV")
                .setMessage("Bạn chắc chắn xoá \"" + c.getName() + "\"?")
                .setNegativeButton("Huỷ", null)
                .setPositiveButton("Xoá", (d, w) -> {
                    api.deleteCoach(new ApiService.IdReq(c.getId()))
                            .enqueue(new Callback<ApiService.SimpleResp>() {
                                @Override public void onResponse(Call<ApiService.SimpleResp> call, Response<ApiService.SimpleResp> r) {
                                    if (r.isSuccessful() && r.body()!=null && r.body().ok) {
                                        Toast.makeText(getContext(), "Đã xoá", Toast.LENGTH_SHORT).show();
                                        String cur = (String) spnSport.getSelectedItem();
                                        loadCoaches("Tất cả".equalsIgnoreCase(cur)? null : cur);
                                    } else {
                                        Toast.makeText(getContext(), "Xoá thất bại", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override public void onFailure(Call<ApiService.SimpleResp> call, Throwable t) {
                                    Toast.makeText(getContext(), "Lỗi mạng: "+t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .show();
    }
}
