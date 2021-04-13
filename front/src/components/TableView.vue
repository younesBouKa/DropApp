<template>
    <div style="height: 100%" ref="table_wrapper">
        <el-table
                ref="table"
                :height="'100%'"
                :data="dataRows"
                @row-dblclick="rowDblClick"
                @row-contextmenu="rowCtxMenu"
                @row-click="rowClick"
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
                            :type="scope.row.permission === 'READ' ? 'primary' : 'success'"
                            size="mini"
                            disable-transitions>{{scope.row.permission}}</el-tag>
                    <el-link v-else-if="scope.column.property==='name'"
                             :underline="false"
                             style="cursor: pointer;"
                             @click="setCurrentNode(scope.row)"
                            disable-transitions>{{scope.row.name}}</el-link>
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
                type : Array,
                default : ()=>[]
            },
            columns : {
                type : Array,
                default : ()=>[]
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
            dataRows() {
                return this.rows;
            },
            dataColumns(){
                let cols= this.columns.length>0
                    ? this.columns
                    : (this.rows.length>0 ? Object.keys(this.rows[0]) : []);
                return cols.map((oldCol,index)=>{
                    let col = {};
                    if(!oldCol.hasOwnProperty("field") || typeof oldCol=== "String")
                        col.field = col;
                    else
                        col = oldCol;
                    if(!oldCol.hasOwnProperty("width"))
                        col.width = (100/cols.length)+"%";
                    if(!oldCol.hasOwnProperty("label"))
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
            formater(row, column, cellValue, index){
                return cellValue+"sdsd";
            },
            filterHandler(value, row, column) {
                const property = column['property'];
                return row[property] === value;
            },
            setCurrentNode(row){
              this.$store.commit("storeCurrentNode",row);
            },
            rowDblClick(row, column, event){
                console.log("rowDblClick : ",row, column, event);
                this.setCurrentNode(row);
            },
            rowCtxMenu(row, column, event){
                console.log("rowCtxMenu : ",row, column, event);
                event.preventDefault()
            },
            rowClick(row, column, event){
                console.log("rowClick : ",row, column, event);
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
    .el-table >>> .col_header{
        font-size: small !important;
    }
</style>
