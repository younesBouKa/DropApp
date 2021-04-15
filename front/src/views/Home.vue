<template>
    <div class="home">
        <el-container style="height: 500px;">
            <el-aside v-show="showAside">
                <SpaceTree :showTopMenu="true" :rootPath="'/'"></SpaceTree>
            </el-aside>
            <el-container style="margin-left: 5px;">
                <el-header >
                    <el-row class="el-header-row">
                        <el-col class="header-left-part">
                            <div>
                                <el-button
                                        @click="showAside=!showAside"
                                        size="mini"
                                        :icon="showAside?'el-icon-d-arrow-left':'el-icon-d-arrow-right'"
                                        :title="showAside?'Collapse':'Expand'"
                                        circle
                                ></el-button>
                            </div>
                        </el-col>
                        <el-col class="header-right-part">
                            <div class="header-buttons">
                                <el-dropdown trigger="click" @command="handleCommand">
                                    <span class="el-dropdown-link">
                                        <el-button
                                                size="mini"
                                                icon="el-icon-menu"
                                                title="Profil"
                                                circle
                                        ></el-button>
                                    </span>
                                    <el-dropdown-menu slot="dropdown">
                                        <!--<div style="height: 200px; width: 200px; background-color: #2c3e50;"></div>-->
                                        <el-dropdown-item icon="el-icon-plus" command="action1">Action 1</el-dropdown-item>
                                        <el-dropdown-item icon="el-icon-circle-plus" command="action2">Action 2</el-dropdown-item>
                                        <el-dropdown-item icon="el-icon-circle-plus-outline" command="action3">Action 3</el-dropdown-item>
                                        <el-dropdown-item icon="el-icon-check" command="action4">Action 4</el-dropdown-item>
                                        <el-dropdown-item icon="el-icon-circle-check" command="action5">Action 5</el-dropdown-item>
                                    </el-dropdown-menu>
                                </el-dropdown>
                            </div>
                        </el-col>
                    </el-row>
                </el-header>

                <el-main>
                    <FolderContent></FolderContent>
                </el-main>
            </el-container>
        </el-container>
        <CreateFolderDialog></CreateFolderDialog>
    </div>
</template>

<script>
    // @ is an alias to /src
    import UploadFiles from '@/components/UploadFiles.vue'
    import SpaceTree from '@/components/SpaceTree.vue'
    import FolderContent from '@/components/FolderContent.vue'
    import CreateFolderDialog from '@/components/CreateFolderDialog.vue'

    import { mapActions,mapGetters } from 'vuex'
    export default {
        name: 'Home',
        components: {
            UploadFiles,
            SpaceTree,
            FolderContent,
            CreateFolderDialog
        },
        data() {
            return {
                enableUpload : false,
                showAside: true,
            }
        },
        computed : {
            isSelectedNodeFolder(){
                return this.$store.getters.getCurrentNodeData.folder;
            },
            ...mapGetters([
                 "getCurrentNodeData"
                ]),
        },
        methods: {
            ...mapActions([
                'getNodeById',
                'getNodeByPath',
                'getNodesByParentPath',
                'getNodesByParentId',
            ]),
            handleCommand(command){
              console.log("handleCommand : ",command);
            },
        },
        mounted() {
            let self = this;
            this.$bus.$on("files_uploaded", function (payLoad) {
                self.enableUpload = false;
            });
        }
    }
</script>

<style>
    .el-menu-item-group__title {
        padding-left: 40px;
        text-align: start;
    }

    .el-main {
        padding: 5px !important;
    }

    .el-header{
        color: #333;
        background-color: #eaedf2;
        padding: 3px 5px !important;
        height: unset !important;
    }

    .el-header-row{
        display: flex;
        flex-direction: row;
    }

    .header-left-part{
        display: flex;
        flex-direction: row;
        justify-content: flex-start;
    }

    .header-right-part{
        display: flex;
        flex-direction: row;
        justify-content: flex-end;
    }

    .el-aside{
        color: #333;
        overflow: hidden !important;
        width: 200px !important;
        height: 100% !important;
    }

    ::-webkit-scrollbar {
        width: 5px;
    }

    /* Track */
    ::-webkit-scrollbar-track {
        background: none;
    }

    /* Handle */
    ::-webkit-scrollbar-thumb {
        background: none;
    }

    /* Handle on hover */
    ::-webkit-scrollbar-thumb:hover {
        background: #cfc8c8;
        width: 10px;
    }
</style>