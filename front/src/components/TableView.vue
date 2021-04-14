<template>
    <div style="height: 100%" ref="table_wrapper">
        <el-table
                ref="table"
                :height="'100%'"
                :data="dataRows"
                @row-dblclick="rowDblClick"
                @row-contextmenu="rowCtxMenu"
                @row-click="rowClick"
                :empty-text="'Empty'"
                :row-class-name="'custom-row'"
                style="width: 100%">
            <el-table-column
                    v-for="(column,index) in dataColumns"
                    :prop="column.field"
                    :label="column.label"
                    sortable
                    :formatter="formater"
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
                    <el-link v-else-if="scope.column.property==='name'"
                             :underline="false"
                             style="cursor: pointer;"
                             :icon="scope.row.type==='FILE' ? 'el-icon-document' : 'el-icon-folder'"
                             @click="setCurrentNode(scope.row)"
                             disable-transitions>{{scope.row.name}}
                    </el-link>
                    <span v-else-if="scope.column.property==='createDate'">
                        <i class="el-icon-date"></i>
                        {{new Date(scope.row.createDate).toLocaleDateString()}}
                    </span>
                    <span v-else>{{scope.row[scope.column.property]}}</span>
                </template>
            </el-table-column>
        </el-table>
    </div>
</template>

<script>
    import {mapActions, mapGetters} from 'vuex'
    import NodeCard from '@/components/NodeCard.vue'

    const _ = require('lodash');
    export default {
        name: "TableView",
        components: {
            NodeCard,
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
                    if (typeof node.permission === "String") {
                        let permissions = node.permission.split("_");
                        node.permission = [];
                        permissions.forEach(per => {
                            per = per.toLowerCase();
                            per = per === "" ? "none" : per;
                            node.permission.push(this.permissionsOptions[per]);
                        })
                    }
                    return node;
                })
                // sort
                return rows.sort(this.sortByFolderFirst);
            },
            dataColumns() {
                let cols = this.columns.length > 0
                    ? this.columns
                    : (this.rows.length > 0 ? Object.keys(this.rows[0]) : []);
                return cols.map((oldCol, index) => {
                    let col = {};
                    if (typeof oldCol === "String" || !oldCol.hasOwnProperty("field"))
                        col.field = oldCol;
                    else
                        col = oldCol;
                    if (!oldCol.hasOwnProperty("width"))
                        col.width = (100 / cols.length) + "%";
                    if (!oldCol.hasOwnProperty("label"))
                        col.label = col.field.toUpperCase();
                    return col;
                })
            },
            /* ...mapGetters({
                 selectedNode:"getCurrentNode"
             }),*/
        },
        watch: {},
        mounted() {
        },
        methods: {
            ...mapActions({
                getNodesByParentId: 'getNodesByParentId',
            }),
            formater(row, column, cellValue, index) {
                return cellValue + "sdsd";
            },
            filterHandler(value, row, column) {
                const property = column['property'];
                return row[property] === value;
            },
            setCurrentNode(row) {
                this.$store.commit("storeCurrentNode", row);
            },
            rowDblClick(row, column, event) {
                console.log("rowDblClick : ", row, column, event);
                this.setCurrentNode(row);
            },
            rowCtxMenu(row, column, event) {
                console.log("rowCtxMenu : ", row, column, event);
                event.preventDefault()
            },
            rowClick(row, column, event) {
                console.log("rowClick : ", row, column, event);
            },
            openFullScreen2() {
                const loading = this.$loading({
                    lock: true,
                    text: 'Loading',
                    spinner: 'el-icon-loading',
                    background: 'rgba(0, 0, 0, 0.7)'
                });
                setTimeout(() => {
                    loading.close();
                }, 2000);
            }
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
</style>
