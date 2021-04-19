<template>
    <div style="height: 100%" ref="table_wrapper">
        <el-table
                ref="table"
                :height="'100%'"
                :data="dataRows"
                @row-dblclick="rowDblClick"
                @row-contextmenu="rowCtxMenu"
                @row-click="rowClick"
                @selection-change="handleSelectionChange"
                :empty-text="'Empty'"
                :row-class-name="'custom-row'"
                :header-row-class-name="'custom-header-row'"
                style="width: 100%">
            <el-table-column
                    type="selection"
                    width="55">
            </el-table-column>
            <el-table-column
                    v-for="(column,index) in dataColumns"
                    :prop="column.field"
                    :label="column.label"
                    sortable
                    :label-class-name="'col_header'"
                    :show-overflow-tooltip="true"
                    :column-key="column.field"
            >
                <template slot-scope="scope">
                    <el-tag v-if="scope.column.property==='permission'"
                            v-for="per in scope.row.permission"
                            :type="per.type"
                            style="margin-left: 3px"
                            size="mini"
                            :title="per.label"
                            disable-transitions>
                        <i :class="per.icon"></i>
                    </el-tag>
                    <el-link v-if="scope.column.property==='name'"
                             :underline="false"
                             style="cursor: pointer;"
                             :icon="scope.row.type==='FILE' ? 'el-icon-document' : 'el-icon-folder'"
                             @click="setCurrentNodeData(scope.row)"
                             disable-transitions>{{scope.row.name}}
                    </el-link>
                    <span v-else-if="scope.column.property==='createDate'">
                        <i class="el-icon-time"></i>
                        {{new Date(scope.row.createDate).toLocaleDateString()}}
                    </span>
                    <span v-else-if="scope.column.property!=='permission'">
                        {{scope.row[scope.column.property]}}
                    </span>
                </template>
            </el-table-column>
        </el-table>
    </div>
</template>

<script>
    import {mapActions} from 'vuex'

    const _ = require('lodash');
    export default {
        name: "TableView",
        components: {
        },
        props: {
            rows: {
                type: Array,
                default: () => []
            },
            columns: {
                type: Array,
                default: () => []
            }
        },
        data() {
            return {
                minColWidth : 100,
                containerWidth : 0,
                selectedRows : [],
                defaultProps: {
                    children: 'children',
                    label: 'name'
                }
            }
        },
        computed: {
            permissionsOptions() {
                return this.$store.getters.getPermissionOptions;
            },
            sortByFolderFirst() {
                return this.$store.getters.getSortByFolderFirst;
            },
            dataRows() {
                let rows = this.rows;
                // build permissions
                rows = rows.map(node => {
                    if (typeof node.permission === "string") {
                        let permissions = node.permission.split("_");
                        node.permission = [];
                        permissions.forEach(per => {
                            per = per.toLowerCase();
                            per = per === "" ? "none" : per;
                            node.permission.push(this.permissionsOptions[per]);
                        })
                    }
                    return node;
                });
                // sort
                return rows.sort(this.sortByFolderFirst);
            },
            dataColumns() {
                let cols = this.columns.length > 0
                    ? this.columns
                    : (this.rows.length > 0 ? Object.keys(this.rows[0]) : []);
                cols = cols.map((oldCol, index) => {
                    let col = {};
                    if (typeof oldCol === "string" || !oldCol.hasOwnProperty("field"))
                        col.field = oldCol;
                    else
                        col = oldCol;
                    if (!oldCol.hasOwnProperty("width"))
                        col.width = (100 / cols.length) + "%";
                    if (!oldCol.hasOwnProperty("label"))
                        col.label = col.field.toUpperCase();
                    return col;
                });
                let nbrOfColumnsToShow = this.containerWidth > this.minColWidth
                    ? parseInt((this.containerWidth/this.minColWidth).toFixed(0) ,10): cols.length;
                return cols.slice(0,nbrOfColumnsToShow);
            },
        },
        watch: {
            dataRows(val){
                this.selectedNodes = [];
            }
        },
        mounted() {
            let self = this;
            this.containerWidth = this.$el.getBoundingClientRect().width;
            window.addEventListener('resize', ()=>{
                try {
                    let containerWidth = self.$el.getBoundingClientRect().width;
                    self.containerWidth = containerWidth > self.minColWidth ? containerWidth : self.containerWidth;
                }catch(error){}
            });
        },
        methods: {
            ...mapActions({
                getNodesByParentId: 'getNodesByParentId',
            }),
            handleSelectionChange(val) {
                this.selectedRows = val;
                this.$emit("selectionChanged",this.selectedRows);
            },
            filterHandler(value, row, column) {
                const property = column['property'];
                return row[property] === value;
            },
            setCurrentNodeData(row) {
                this.$store.commit("storeCurrentNodeData", row);
            },
            rowDblClick(row, column, event) {
                console.log("rowDblClick : ", row, column, event);
                this.setCurrentNodeData(row);
            },
            rowCtxMenu(row, column, event) {
                console.log("rowCtxMenu : ", row, column, event);
                event.preventDefault()
            },
            rowClick(row, column, event) {
                console.log("rowClick : ", row, column, event);
            },
        }
    }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
    .el-table >>> .col_header {
        font-size: 10px;
    }

    .el-table >>> td {
        font-size: 10px;
        padding: 3px;
    }

    .el-table >>> .el-link--inner {
        font-size: 11px;
    }

    .el-table >>> .custom-row {
        height: 30px; /* .el-table >>> td {padding} */
    }

    .el-table >>> .custom-header-row {
        height: 30px; /* .el-table >>> td {padding} */
    }
</style>
