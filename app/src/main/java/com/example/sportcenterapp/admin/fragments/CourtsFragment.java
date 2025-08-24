package com.example.sportcenterapp.admin.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.sportcenterapp.R;
import com.example.sportcenterapp.adapters.CourtAdapter;
import com.example.sportcenterapp.models.Court;
import com.example.sportcenterapp.net.ApiClient;
import com.example.sportcenterapp.net.ApiService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.Okio;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourtsFragment extends Fragment implements CourtAdapter.Actions {

    private RecyclerView rv;
    private SwipeRefreshLayout swipe;
    private CourtAdapter adapter;
    private final List<Court> data = new ArrayList<>();
    private ApiService api;

    // ==== Picker ảnh ====
    private Court editingCourt = null;   // court đang sửa (hoặc null nếu thêm)
    private Uri pickedImage = null;      // ảnh vừa chọn (để upload sau khi lưu)
    private ImageView currentPreview;    // giữ reference đến ivPreview trong dialog

    private final ActivityResultLauncher<Intent> pickImage =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
                if (res.getResultCode()== Activity.RESULT_OK && res.getData()!=null) {
                    pickedImage = res.getData().getData();
                    if (currentPreview != null) {
                        currentPreview.setImageURI(pickedImage); // xem trước ảnh mới
                    }
                }
            });

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = inf.inflate(R.layout.fragment_admin_courts, c, false);
        rv    = v.findViewById(R.id.rvCourts);
        swipe = v.findViewById(R.id.swipeRefresh);
        Button btnAdd = v.findViewById(R.id.btnAddCourt);
        btnAdd.setOnClickListener(v1 -> showEditDialog(null)); // null = thêm mới


        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CourtAdapter(data, this);
        rv.setAdapter(adapter);

        // Vuốt trái/phải để xoá
        attachSwipeToDelete();

        api = ApiClient.get().create(ApiService.class);
        if (swipe != null) swipe.setOnRefreshListener(this::loadCourts);
        loadCourts();
        return v;
    }

    private void attachSwipeToDelete() {
        ItemTouchHelper.SimpleCallback cb = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder v, @NonNull RecyclerView.ViewHolder t) { return false; }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getAdapterPosition();
                if (pos < 0 || pos >= data.size()) return;
                Court c = data.get(pos);
                confirmDelete(c, pos);
            }
        };
        new ItemTouchHelper(cb).attachToRecyclerView(rv);
    }

    private void confirmDelete(Court c, int pos) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xoá sân")
                .setMessage("Xoá \"" + (c.name!=null?c.name:("Sân #"+c.id)) + "\"?")
                .setPositiveButton("Xoá", (d,w)-> doDelete(c, pos))
                .setNegativeButton("Huỷ", (d,w)-> adapter.notifyItemChanged(pos)) // restore item nếu huỷ
                .show();
    }

    private void doDelete(Court c, int pos) {
        api.deleteCourt(new ApiService.IdReq(c.id)).enqueue(new Callback<ApiService.SimpleResp>() {
            @Override public void onResponse(Call<ApiService.SimpleResp> call, Response<ApiService.SimpleResp> r) {
                if (r.isSuccessful() && r.body()!=null && r.body().ok) {
                    data.remove(pos);
                    adapter.notifyItemRemoved(pos);
                    Toast.makeText(getContext(),"Đã xoá",Toast.LENGTH_SHORT).show();
                } else {
                    adapter.notifyItemChanged(pos); // restore
                    Toast.makeText(getContext(), r.body()!=null? String.valueOf(r.body().error) : "Xoá thất bại", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<ApiService.SimpleResp> call, Throwable t) {
                adapter.notifyItemChanged(pos);
                Toast.makeText(getContext(),"Lỗi mạng: "+t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---- LOAD LIST ----
    private void loadCourts() {
        if (swipe != null) swipe.setRefreshing(true);
        api.getCourts(null).enqueue(new Callback<List<Court>>() {
            @Override public void onResponse(Call<List<Court>> call, Response<List<Court>> r) {
                if (swipe != null) swipe.setRefreshing(false);
                if (!r.isSuccessful() || r.body()==null) { Toast.makeText(getContext(),"Không tải được",Toast.LENGTH_SHORT).show(); return; }
                data.clear(); data.addAll(r.body()); adapter.notifyDataSetChanged();
            }
            @Override public void onFailure(Call<List<Court>> call, Throwable t) {
                if (swipe != null) swipe.setRefreshing(false);
                Toast.makeText(getContext(),"Lỗi mạng: "+t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override public void onEdit(Court c) { showEditDialog(c); }
    @Override public void onDelete(Court c) { /* đã xử lý bằng swipe ở trên, giữ trống */ }

    private void showEditDialog(@Nullable Court court) {
        editingCourt = court;
        pickedImage = null;

        View d = LayoutInflater.from(getContext()).inflate(R.layout.dialog_admin_court, null, false);
        currentPreview = d.findViewById(R.id.ivPreview);
        EditText etName = d.findViewById(R.id.etName);
        EditText etSport = d.findViewById(R.id.etSport);
        EditText etSurface = d.findViewById(R.id.etSurface);
        Switch swIndoor = d.findViewById(R.id.swIndoor);
        EditText etPrice = d.findViewById(R.id.etPrice);
        EditText etDesc  = d.findViewById(R.id.etDescription);
        EditText etRating= d.findViewById(R.id.etRating);
        TextView tvHint  = d.findViewById(R.id.tvHint);

        // Prefill
        if (court != null) {
            etName.setText(court.name);
            etSport.setText(court.sport);
            etSurface.setText(court.surface);
            swIndoor.setChecked(court.indoor==1);
            etPrice.setText(String.valueOf(court.price)); // sửa đúng field
            etDesc.setText(court.description!=null?court.description:"");
            etRating.setText(String.valueOf(court.rating));

            // load ảnh cũ
            String img = court.image;
            if (img != null && !img.isEmpty()) {
                if (!img.startsWith("http")) img = ApiClient.BASE_URL + (img.startsWith("/")? img.substring(1):img);
                Glide.with(currentPreview.getContext())
                        .load(img).placeholder(R.drawable.placeholder_court)
                        .error(R.drawable.placeholder_court).into(currentPreview);
            } else {
                currentPreview.setImageResource(R.drawable.placeholder_court);
            }
        } else {
            currentPreview.setImageResource(R.drawable.placeholder_court);
        }

        // Bấm ảnh để chọn ảnh mới
        currentPreview.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            pickImage.launch(i);
        });

        new AlertDialog.Builder(requireContext())
                .setTitle(court==null ? "Thêm sân" : "Sửa sân")
                .setView(d)
                .setPositiveButton("Lưu", (dlg, w) -> {
                    ApiService.CourtSaveReq req = new ApiService.CourtSaveReq();
                    req.id = court==null ? null : court.id;
                    req.name = etName.getText().toString().trim();
                    req.sport = etSport.getText().toString().trim();
                    req.surface = etSurface.getText().toString().trim();
                    req.indoor = swIndoor.isChecked()?1:0;
                    req.price = parseD(etPrice.getText().toString());
                    req.description = etDesc.getText().toString().trim();
                    req.rating = parseD(etRating.getText().toString());

                    if (req.name.isEmpty()) { Toast.makeText(getContext(),"Tên sân không được trống",Toast.LENGTH_SHORT).show(); return; }

                    api.saveCourt(req).enqueue(new Callback<ApiService.SimpleRespId>() {
                        @Override public void onResponse(Call<ApiService.SimpleRespId> c, Response<ApiService.SimpleRespId> r) {
                            if (r.isSuccessful() && r.body()!=null && r.body().ok) {
                                int courtId = (req.id!=null) ? req.id : r.body().id;
                                if (pickedImage != null) {
                                    uploadCourtImageThenRefresh(courtId, pickedImage);
                                } else {
                                    Toast.makeText(getContext(),"Đã lưu",Toast.LENGTH_SHORT).show();
                                    loadCourts();
                                }
                            } else {
                                Toast.makeText(getContext(),"Lưu thất bại",Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override public void onFailure(Call<ApiService.SimpleRespId> c, Throwable t) {
                            Toast.makeText(getContext(),"Lỗi mạng: "+t.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void uploadCourtImageThenRefresh(int courtId, Uri uri) {
        try {
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            byte[] bytes = Okio.buffer(Okio.source(is)).readByteArray();

            RequestBody idPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(courtId));
            RequestBody imgBody= RequestBody.create(MediaType.parse("image/*"), bytes);
            MultipartBody.Part imgPart = MultipartBody.Part.createFormData("image","court.jpg", imgBody);

            api.uploadCourtImage(idPart, imgPart).enqueue(new Callback<ApiService.UploadImageResp>() {
                @Override public void onResponse(Call<ApiService.UploadImageResp> c, Response<ApiService.UploadImageResp> r) {
                    if (r.isSuccessful() && r.body()!=null && r.body().ok) {
                        Toast.makeText(getContext(),"Đã lưu & cập nhật ảnh",Toast.LENGTH_SHORT).show();
                        loadCourts();
                    } else {
                        Toast.makeText(getContext(),"Upload ảnh thất bại",Toast.LENGTH_SHORT).show();
                        loadCourts();
                    }
                }
                @Override public void onFailure(Call<ApiService.UploadImageResp> c, Throwable t) {
                    Toast.makeText(getContext(),"Lỗi upload: "+t.getMessage(),Toast.LENGTH_SHORT).show();
                    loadCourts();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(),"Không đọc được ảnh: "+e.getMessage(),Toast.LENGTH_SHORT).show();
            loadCourts();
        }
    }

    private double parseD(String s){ try { return Double.parseDouble(s); } catch(Exception e){ return 0; } }
}
