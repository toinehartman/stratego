package strategolib.strategies;

import org.strategoxt.lang.JavaInteropRegisterer;
import org.strategoxt.lang.Strategy;

public class InteropRegisterer extends JavaInteropRegisterer {
    public InteropRegisterer() {
        super(new Strategy[] {
            // @formatter:off
            $E$X$D$E$V_0_0.instance,
            $P__tmpdir_0_0.instance,
            $S$T$D$E$R$R__$F$I$L$E$N$O_0_0.instance,
            $S$T$D$I$N__$F$I$L$E$N$O_0_0.instance,
            $S$T$D$O$U$T__$F$I$L$E$N$O_0_0.instance,
            access_0_1.instance,
            address_0_0.instance,
            apply_sref_0_1.instance,
            at_end_1_0.instance,
            atan2_0_1.instance,
            catch_with_2_0.instance,
            chdir_0_0.instance,
            checksum_0_0.instance,
            clock_0_0.instance,
            clock_to_seconds_0_0.instance,
            close_0_0.instance,
            concat_0_0.instance,
            concat_strings_0_0.instance,
            cos_0_0.instance,
            creat_0_0.instance,
            crush_3_0.instance,
            dr_rule_sets_hashtable_0_0.instance,
            dtime_0_0.instance,
            dup_0_0.instance,
            dup2_0_1.instance,
            exit_0_0.instance,
            explode_string_0_0.instance,
            explode_term_0_0.instance,
            fatal_err_0_3.instance,
            fgetc_0_0.instance,
            filemode_0_0.instance,
            filter_1_0.instance,
            flatten_list_0_0.instance,
            fork_0_0.instance,
            get_appl_arguments_1_0.instance,
            get_arguments_0_0.instance,
            get_constructor_0_0.instance,
            get_errno_0_0.instance,
            get_pid_0_0.instance,
            get_placeholder_0_0.instance,
            get_random_max_0_0.instance,
            getcwd_0_0.instance,
            getenv_0_0.instance,
            implode_string_0_0.instance,
            int_0_0.instance,
            int_add_0_1.instance,
            int_and_0_1.instance,
            int_div_0_1.instance,
            int_gt_0_1.instance,
            int_ior_0_1.instance,
            int_mod_0_1.instance,
            int_mul_0_1.instance,
            int_shl_0_1.instance,
            int_shr_0_1.instance,
            int_subt_0_1.instance,
            int_to_string_0_0.instance,
            int_xor_0_1.instance,
            internal_call_0_4.instance,
            internal_copy_file_0_1.instance,
            internal_epoch2localtime_0_0.instance,
            internal_epoch2utc_0_0.instance,
            internal_execv_0_1.instance,
            internal_execvp_0_1.instance,
            internal_fclose_0_0.instance,
            internal_fdcopy_0_1.instance,
            internal_fdopen_0_1.instance,
            internal_fflush_0_0.instance,
            internal_fopen_0_1.instance,
            internal_fputc_0_1.instance,
            internal_fputs_0_1.instance,
            internal_hashtable_create_0_1.instance,
            internal_hashtable_destroy_0_0.instance,
            internal_hashtable_fold_1_1.instance,
            internal_hashtable_get_0_1.instance,
            internal_hashtable_keys_0_0.instance,
            internal_hashtable_keys_fold_1_1.instance,
            internal_hashtable_put_0_2.instance,
            internal_hashtable_remove_0_1.instance,
            internal_hashtable_reset_0_0.instance,
            internal_hashtable_values_fold_1_1.instance,
            internal_immutable_map_0_0.instance,
            internal_immutable_map_filter_2_0.instance,
            internal_immutable_map_from_list_1_0.instance,
            internal_immutable_map_get_0_1.instance,
            internal_immutable_map_get_eq_1_1.instance,
            internal_immutable_map_intersect_1_1.instance,
            internal_immutable_map_intersect_eq_2_1.instance,
            internal_immutable_map_intersect_set_0_1.instance,
            internal_immutable_map_intersect_set_eq_1_1.instance,
            internal_immutable_map_keys_0_0.instance,
            internal_immutable_map_keys_to_set_0_0.instance,
            internal_immutable_map_map_2_0.instance,
            internal_immutable_map_put_0_2.instance,
            internal_immutable_map_put_eq_1_2.instance,
            internal_immutable_map_remove_0_1.instance,
            internal_immutable_map_remove_eq_1_1.instance,
            internal_immutable_map_subtract_0_1.instance,
            internal_immutable_map_subtract_eq_1_1.instance,
            internal_immutable_map_subtract_set_0_1.instance,
            internal_immutable_map_subtract_set_eq_1_1.instance,
            internal_immutable_map_to_list_0_0.instance,
            internal_immutable_map_to_relation_0_0.instance,
            internal_immutable_map_union_1_1.instance,
            internal_immutable_map_union_eq_2_1.instance,
            internal_immutable_map_values_0_0.instance,
            internal_immutable_relation_0_0.instance,
            internal_immutable_relation_compose_0_1.instance,
            internal_immutable_relation_contains_0_2.instance,
            internal_immutable_relation_filter_1_0.instance,
            internal_immutable_relation_from_list_0_0.instance,
            internal_immutable_relation_get_0_1.instance,
            internal_immutable_relation_insert_0_2.instance,
            internal_immutable_relation_intersect_0_1.instance,
            internal_immutable_relation_inverse_0_0.instance,
            internal_immutable_relation_keys_0_0.instance,
            internal_immutable_relation_keys_set_0_0.instance,
            internal_immutable_relation_map_1_0.instance,
            internal_immutable_relation_reflexive_transitive_closure_0_0.instance,
            internal_immutable_relation_remove_0_1.instance,
            internal_immutable_relation_subtract_0_1.instance,
            internal_immutable_relation_to_list_0_0.instance,
            internal_immutable_relation_to_map_1_0.instance,
            internal_immutable_relation_to_set_0_0.instance,
            internal_immutable_relation_transitive_closure_0_0.instance,
            internal_immutable_relation_union_0_1.instance,
            internal_immutable_relation_values_0_0.instance,
            internal_immutable_relation_values_set_0_0.instance,
            internal_immutable_set_0_0.instance,
            internal_immutable_set_cartesian_product_0_1.instance,
            internal_immutable_set_contains_0_1.instance,
            internal_immutable_set_contains_eq_1_1.instance,
            internal_immutable_set_elements_0_0.instance,
            internal_immutable_set_filter_1_0.instance,
            internal_immutable_set_from_list_0_0.instance,
            internal_immutable_set_insert_0_1.instance,
            internal_immutable_set_insert_eq_1_1.instance,
            internal_immutable_set_intersect_0_1.instance,
            internal_immutable_set_intersect_eq_1_1.instance,
            internal_immutable_set_map_2_0.instance,
            internal_immutable_set_remove_0_1.instance,
            internal_immutable_set_remove_eq_1_1.instance,
            internal_immutable_set_strict_subset_0_1.instance,
            internal_immutable_set_strict_subset_eq_1_1.instance,
            internal_immutable_set_subtract_0_1.instance,
            internal_immutable_set_subtract_eq_1_1.instance,
            internal_immutable_set_union_0_1.instance,
            internal_immutable_set_union_eq_1_1.instance,
            internal_indexed_set_create_0_1.instance,
            internal_indexed_set_destroy_0_0.instance,
            internal_indexed_set_elements_0_0.instance,
            internal_indexed_set_get_elem_0_1.instance,
            internal_indexed_set_get_index_0_1.instance,
            internal_indexed_set_put_1_1.instance,
            internal_indexed_set_remove_0_1.instance,
            internal_indexed_set_reset_0_0.instance,
            internal_java_call_0_2.instance,
            internal_kill_0_1.instance,
            internal_mkdtemp_0_0.instance,
            internal_mkstemp_0_0.instance,
            internal_mkterm_0_1.instance,
            internal_read_term_from_stream_0_0.instance,
            internal_read_text_from_stream_0_0.instance,
            internal_rename_file_0_1.instance,
            internal_setenv_0_2.instance,
            internal_table_hashtable_0_0.instance,
            internal_write_term_to_stream_baf_0_1.instance,
            internal_write_term_to_stream_saf_0_1.instance,
            internal_write_term_to_stream_taf_0_1.instance,
            internal_write_term_to_stream_text_0_1.instance,
            is_int_0_0.instance,
            is_placeholder_0_0.instance,
            is_real_0_0.instance,
            is_string_0_0.instance,
            isatty_0_0.instance,
            isblk_0_0.instance,
            ischr_0_0.instance,
            isdir_0_0.instance,
            isfifo_0_0.instance,
            islnk_0_0.instance,
            isreg_0_0.instance,
            issock_0_0.instance,
            length_0_0.instance,
            link_file_0_1.instance,
            list_fold_1_1.instance,
            list_loop_1_0.instance,
            make_placeholder_0_0.instance,
            mkdir_0_1.instance,
            modification_time_0_0.instance,
            new_0_0.instance,
            newname_0_0.instance,
            newterm_0_0.instance,
            next_random_0_0.instance,
            open_0_0.instance,
            perror_0_0.instance,
            pipe_0_0.instance,
            read_from_string_0_0.instance,
            readdir_0_0.instance,
            real_0_0.instance,
            real_add_0_1.instance,
            real_div_0_1.instance,
            real_gt_0_1.instance,
            real_mod_0_1.instance,
            real_mul_0_1.instance,
            real_subt_0_1.instance,
            real_to_string_0_0.instance,
            real_to_string_0_1.instance,
            remove_file_0_0.instance,
            repeat_2_0.instance,
            rmdir_0_0.instance,
            set_random_seed_0_0.instance,
            sin_0_0.instance,
            sqrt_0_0.instance,
            stacktrace_get_all_frame_names_0_0.instance,
            stacktrace_get_current_frame_index_0_0.instance,
            stacktrace_get_current_frame_name_0_0.instance,
            stderr_stream_0_0.instance,
            stdin_stream_0_0.instance,
            stdout_stream_0_0.instance,
            strcat_0_1.instance,
            strerror_0_0.instance,
            string_replace_0_2.instance,
            string_to_int_0_0.instance,
            string_to_real_0_0.instance,
            strlen_0_0.instance,
            term_address_lt_0_1.instance,
            ticks_to_seconds_0_0.instance,
            time_0_0.instance,
            times_0_0.instance,
            tmpnam_0_0.instance,
            to_sref_1_0.instance,
            waitpid_0_0.instance,
            write_to_binary_string_0_0.instance,
            write_to_shared_string_0_0.instance,
            write_to_string_0_0.instance
            // @formatter:on
        });
    }
}
