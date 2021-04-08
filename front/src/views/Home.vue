<template>
    <div class="home">
        <el-container style="height: 500px; border: 1px solid #eee">
            <el-aside width="200px" style="background-color: rgb(238, 241, 246)">
                <SpaceTree></SpaceTree>
            </el-aside>
            <el-container>
                <el-header style="text-align: right; font-size: 12px">
                    <el-dropdown>
                        <i class="el-icon-setting" style="margin-right: 15px"></i>
                        <el-dropdown-menu slot="dropdown">
                            <el-dropdown-item>View</el-dropdown-item>
                            <el-dropdown-item>Add</el-dropdown-item>
                            <el-dropdown-item>Delete</el-dropdown-item>
                        </el-dropdown-menu>
                    </el-dropdown>
                    <span>Tom</span>
                </el-header>

                <el-main>
                    <UploadFiles></UploadFiles>
                </el-main>
            </el-container>
        </el-container>
    </div>
</template>

<script>
    // @ is an alias to /src
    import UploadFiles from '@/components/UploadFiles.vue'
    import SpaceTree from '@/components/SpaceTree.vue'

    export default {
        name: 'Home',
        components: {
            UploadFiles,
            SpaceTree
        },
        data() {
            return {

            }
        },
        methods: {

        },
        mounted() {
            fetch("/api/greeting")
                .then(response => response.json())
                .then((response) => {
                    this.$notify({
                        title: 'Success',
                        message: 'Ceci est un message de succès ' + JSON.stringify(response),
                        type: 'success'
                    });
                })
                .catch(function(err) {
                  console.log('Fetch Error :-S', err);
                });;
        }
    }
</script>

<style>
    .el-header {
        background-color: #B3C0D1;
        color: #333;
        line-height: 60px;
    }

    .el-aside {
        color: #333;
    }

    .el-menu-item-group__title {
        padding-left: 40px;
        text-align: start;
    }

    .el-main {
        padding: 6px !important;
    }

</style>