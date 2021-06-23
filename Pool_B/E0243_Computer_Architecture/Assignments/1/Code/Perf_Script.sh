source shrc

go xz_r

cd run/run_base_refrate_hpca-m64.0000/

sudo perf stat -e CPU_CLK_UNHALTED.THREAD_P,INST_RETIRED.ANY,icache.misses,dtlb_load_misses.miss_causes_a_walk,itlb_misses.miss_causes_a_walk,br_misp_exec.all_branches,l1d.replacement,L2_RQSTS.MISS,LONGEST_LAT_CACHE.MISS -I 299 -x, -o /home/aman/Desktop/Perf/TEST/i11.csv bash run.sh

go deepsjeng_r

cd run/run_base_refrate_hpca-m64.0000/

sudo perf stat -e CPU_CLK_UNHALTED.THREAD_P,INST_RETIRED.ANY,icache.misses,dtlb_load_misses.miss_causes_a_walk,itlb_misses.miss_causes_a_walk,br_misp_exec.all_branches,l1d.replacement,L2_RQSTS.MISS,LONGEST_LAT_CACHE.MISS -I 299 -x, -o /home/aman/Desktop/Perf/TEST/i21.csv bash run.sh

go xalancbmk_r

cd run/run_base_refrate_hpca-m64.0000/

sudo perf stat -e CPU_CLK_UNHALTED.THREAD_P,INST_RETIRED.ANY,icache.misses,dtlb_load_misses.miss_causes_a_walk,itlb_misses.miss_causes_a_walk,br_misp_exec.all_branches,l1d.replacement,L2_RQSTS.MISS,LONGEST_LAT_CACHE.MISS -I 299 -x, -o /home/aman/Desktop/Perf/TEST/i31.csv bash run.sh

go leela_r

cd run/run_base_refrate_hpca-m64.0000/

sudo perf stat -e CPU_CLK_UNHALTED.THREAD_P,INST_RETIRED.ANY,icache.misses,dtlb_load_misses.miss_causes_a_walk,itlb_misses.miss_causes_a_walk,br_misp_exec.all_branches,l1d.replacement,L2_RQSTS.MISS,LONGEST_LAT_CACHE.MISS -I 299 -x, -o /home/aman/Desktop/Perf/TEST/i41.csv bash run.sh

go omnetpp_r

cd run/run_base_refrate_hpca-m64.0000/

sudo perf stat -e CPU_CLK_UNHALTED.THREAD_P,INST_RETIRED.ANY,icache.misses,dtlb_load_misses.miss_causes_a_walk,itlb_misses.miss_causes_a_walk,br_misp_exec.all_branches,l1d.replacement,L2_RQSTS.MISS,LONGEST_LAT_CACHE.MISS -I 299 -x, -o /home/aman/Desktop/Perf/TEST/i51.csv bash run.sh

go namd_r

cd run/run_base_refrate_hpca-m64.0000/

sudo perf stat -e CPU_CLK_UNHALTED.THREAD_P,INST_RETIRED.ANY,icache.misses,dtlb_load_misses.miss_causes_a_walk,itlb_misses.miss_causes_a_walk,br_misp_exec.all_branches,l1d.replacement,L2_RQSTS.MISS,LONGEST_LAT_CACHE.MISS -I 299 -x, -o /home/aman/Desktop/Perf/TEST/f11.csv bash run.sh

go blender_r

cd run/run_base_refrate_hpca-m64.0000/

sudo perf stat -e CPU_CLK_UNHALTED.THREAD_P,INST_RETIRED.ANY,icache.misses,dtlb_load_misses.miss_causes_a_walk,itlb_misses.miss_causes_a_walk,br_misp_exec.all_branches,l1d.replacement,L2_RQSTS.MISS,LONGEST_LAT_CACHE.MISS -I 299 -x, -o /home/aman/Desktop/Perf/TEST/f21.csv bash run.sh

go cactuBSSN_r	

cd run/run_base_refrate_hpca-m64.0000/

sudo perf stat -e CPU_CLK_UNHALTED.THREAD_P,INST_RETIRED.ANY,icache.misses,dtlb_load_misses.miss_causes_a_walk,itlb_misses.miss_causes_a_walk,br_misp_exec.all_branches,l1d.replacement,L2_RQSTS.MISS,LONGEST_LAT_CACHE.MISS -I 299 -x, -o /home/aman/Desktop/Perf/TEST/f31.csv bash run.sh

go povray_r

cd run/run_base_refrate_hpca-m64.0000/

sudo perf stat -e CPU_CLK_UNHALTED.THREAD_P,INST_RETIRED.ANY,icache.misses,dtlb_load_misses.miss_causes_a_walk,itlb_misses.miss_causes_a_walk,br_misp_exec.all_branches,l1d.replacement,L2_RQSTS.MISS,LONGEST_LAT_CACHE.MISS -I 299 -x, -o /home/aman/Desktop/Perf/TEST/f41.csv bash run.sh

go nab_r

cd run/run_base_refrate_hpca-m64.0000/

sudo perf stat -e CPU_CLK_UNHALTED.THREAD_P,INST_RETIRED.ANY,icache.misses,dtlb_load_misses.miss_causes_a_walk,itlb_misses.miss_causes_a_walk,br_misp_exec.all_branches,l1d.replacement,L2_RQSTS.MISS,LONGEST_LAT_CACHE.MISS -I 299 -x, -o /home/aman/Desktop/Perf/TEST/f51.csv bash run.sh
