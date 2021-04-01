<template>
    <div class="home">
        <!--<img alt="Vue logo" src="../assets/logo.png">
        <HelloWorld msg="Welcome to Your Vue.js App"/>-->
        <el-container style="height: 500px; border: 1px solid #eee">
            <el-aside width="200px" style="background-color: rgb(238, 241, 246)">
                <el-menu :default-openeds="['1']">
                    <el-submenu index="1">
                        <template slot="title"><i class="el-icon-message"></i>Navigator One</template>
                        <el-menu-item index="0">Option 0</el-menu-item>
                        <el-menu-item-group>
                            <template slot="title">Group 1</template>
                            <el-menu-item index="1-1">Option 1</el-menu-item>
                            <el-menu-item index="1-2">Option 2</el-menu-item>
                        </el-menu-item-group>
                        <el-menu-item-group title="Group 2">
                            <el-menu-item index="1-3">Option 3</el-menu-item>
                        </el-menu-item-group>
                        <el-submenu index="1-4">
                            <template slot="title">Option4</template>
                            <el-menu-item index="1-4-1">Option 4-1</el-menu-item>
                        </el-submenu>
                    </el-submenu>
                    <el-submenu index="2">
                        <template slot="title"><i class="el-icon-menu"></i>Navigator Two</template>
                        <el-menu-item-group>
                            <template slot="title">Group 1</template>
                            <el-menu-item index="2-1">Option 1</el-menu-item>
                            <el-menu-item index="2-2">Option 2</el-menu-item>
                        </el-menu-item-group>
                        <el-menu-item-group title="Group 2">
                            <el-menu-item index="2-3">Option 3</el-menu-item>
                        </el-menu-item-group>
                        <el-submenu index="2-4">
                            <template slot="title">Option 4</template>
                            <el-menu-item index="2-4-1">Option 4-1</el-menu-item>
                        </el-submenu>
                    </el-submenu>
                    <el-submenu index="3">
                        <template slot="title"><i class="el-icon-setting"></i>Navigator Three</template>
                        <el-menu-item-group>
                            <template slot="title">Group 1</template>
                            <el-menu-item index="3-1">Option 1</el-menu-item>
                            <el-menu-item index="3-2">Option 2</el-menu-item>
                        </el-menu-item-group>
                        <el-menu-item-group title="Group 2">
                            <el-menu-item index="3-3">Option 3</el-menu-item>
                        </el-menu-item-group>
                        <el-submenu index="3-4">
                            <template slot="title">Option 4</template>
                            <el-menu-item index="3-4-1">Option 4-1</el-menu-item>
                        </el-submenu>
                    </el-submenu>
                </el-menu>
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
                    <!--<el-table :data="tableData">
                      <el-table-column prop="date" label="Date" width="140">
                      </el-table-column>
                      <el-table-column prop="name" label="Name" width="120">
                      </el-table-column>
                      <el-table-column prop="address" label="Address">
                      </el-table-column>
                    </el-table>-->
                    <el-upload
                            class="upload-demo"
                            drag
                            action="/api/uploadFile"
                            :on-preview="handlePreview"
                            :on-remove="handleRemove"
                            :before-upload="beforeUpload"
                            :before-remove="beforeRemove"
                            :limit="3"
                            :on-exceed="handleExceed"
                            :file-list="fileList"
                            multiple>
                        <i class="el-icon-upload"></i>
                        <div class="el-upload__text">Déposer les fichiers ici ou<em>cliquez pour envoyer</em></div>
                        <div class="el-upload__tip" slot="tip">Fichiers jpg/png avec une taille inférieure à 500kb</div>
                    </el-upload>
                </el-main>
            </el-container>
        </el-container>
    </div>
</template>

<script>
    // @ is an alias to /src
    import HelloWorld from '@/components/HelloWorld.vue'

    export default {
        name: 'Home',
        components: {
            HelloWorld
        },
        data() {
            const item = {
                date: '2016-05-02',
                name: 'Tom',
                address: 'No. 189, Grove St, Los Angeles'
            };
            return {
                tableData: Array(20).fill(item),
                fileList: [
                    {
                        name: 'food.jpeg',
                        url: 'https://fuss10.elemecdn.com/3/63/4e7f3a15429bfda99bce42a18cdd1jpeg.jpeg?imageMogr2/thumbnail/360x360/format/webp/quality/100'
                    }, {
                        name: 'food2.jpeg',
                        url: 'https://fuss10.elemecdn.com/3/63/4e7f3a15429bfda99bce42a18cdd1jpeg.jpeg?imageMogr2/thumbnail/360x360/format/webp/quality/100'
                    }
                ]
            }
        },
        methods: {
            open() {
                this.$confirm('Ceci effacera le fichier. Continuer?', 'Warning', {
                    confirmButtonText: 'OK',
                    cancelButtonText: 'Annuler',
                    type: 'warning'
                }).then(() => {
                    this.$message({
                        type: 'success',
                        message: 'Fichier supprimé'
                    });
                }).catch(() => {
                    this.$message({
                        type: 'info',
                        message: 'Suppression annulée'
                    });
                });
            },
            beforeUpload(file) {
                console.log(file);
                return true;
            },
            handleRemove(file, fileList) {
                console.log(file, fileList);
            },
            handlePreview(file) {
                console.log(file);
            },
            handleExceed(files, fileList) {
                this.$message.warning(`La limite est 3, vous avez choisi ${files.length} fichiers, soit ${files.length + fileList.length} au total.`);
            },
            beforeRemove(file, fileList) {
                return this.$confirm(`Supprimer le transfert de ${file.name} ?`);
            }
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
</style>