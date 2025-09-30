<template>
  <div class="manage-user-container">
    <a-card title="用户管理" class="user-card">
      <template #extra>
        <a-button type="primary" @click="showAddUserModal">
          <icon-plus />
          添加用户
        </a-button>
      </template>

      <!-- 搜索区域 -->
      <div class="search-area">
        <a-input-search
          v-model="searchText"
          placeholder="搜索用户账号或用户名"
          style="width: 300px"
          @search="handleSearch"
        />
        <a-button @click="resetSearch" style="margin-left: 10px">重置</a-button>
      </div>

      <!-- 用户列表 -->
      <a-table
        :columns="columns"
        :data="userList"
        :pagination="pagination"
        :loading="loading"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      >
        <template #userRole="{ record }">
          <a-tag :color="getRoleColor(record.userRole)">
            {{ getRoleText(record.userRole) }}
          </a-tag>
        </template>

        <template #createTime="{ record }">
          {{ formatDate(record.createTime) }}
        </template>

        <template #operations="{ record }">
          <a-button
            type="text"
            size="small"
            @click="showEditUserModal(record)"
          >
            编辑
          </a-button>
          <a-button
            type="text"
            size="small"
            status="danger"
            @click="showDeleteUserModal(record)"
          >
            删除
          </a-button>
        </template>
      </a-table>
    </a-card>

    <!-- 添加/编辑用户模态框 -->
    <a-modal
      v-model:visible="userModalVisible"
      :title="isEdit ? '编辑用户' : '添加用户'"
      width="600px"
      @ok="handleSubmitUser"
      @cancel="handleCancelUser"
    >
      <a-form :model="userForm" :label-col-props="{ span: 6 }" :wrapper-col-props="{ span: 18 }">
        <a-form-item label="用户账号" required>
          <a-input v-model="userForm.userAccount" placeholder="请输入用户账号" />
        </a-form-item>
        <a-form-item label="用户名" required>
          <a-input v-model="userForm.userName" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="用户密码" :required="!isEdit">
          <a-input-password v-model="userForm.userPassword" placeholder="请输入用户密码" />
        </a-form-item>
        <a-form-item label="用户角色" required>
          <a-select v-model="userForm.userRole" placeholder="请选择用户角色">
            <a-option value="user">普通用户</a-option>
            <a-option value="admin">管理员</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="用户简介">
          <a-textarea v-model="userForm.userProfile" placeholder="请输入用户简介" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 删除确认模态框 -->
    <a-modal
      v-model:visible="deleteModalVisible"
      title="确认删除"
      @ok="handleDeleteUser"
      @cancel="deleteModalVisible = false"
    >
      <p>确定要删除用户 "{{ currentUser?.userAccount }}" 吗？此操作不可恢复。</p>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Message } from '@arco-design/web-vue'
import { IconPlus } from '@arco-design/web-vue/es/icon'
import { getUserList, addUser, updateUser, deleteUser } from '@/api/user/userApi'
import type { UserVO, UserAddRequest, UserUpdateRequest } from '@/api/user/type'

// 响应式数据
const loading = ref(false)
const userList = ref<UserVO[]>([])
const searchText = ref('')
const userModalVisible = ref(false)
const deleteModalVisible = ref(false)
const isEdit = ref(false)
const currentUser = ref<UserVO | null>(null)

// 分页配置
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showPageSize: true,
})

// 用户表单
const userForm = ref({
  userAccount: '',
  userName: '',
  userPassword: '',
  userRole: 'user',
  userProfile: '',
})

// 表格列配置
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    width: 100,
  },
  {
    title: '用户账号',
    dataIndex: 'userAccount',
    width: 150,
  },
  {
    title: '用户名',
    dataIndex: 'userName',
    width: 150,
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
    slotName: 'userRole',
    width: 100,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    slotName: 'createTime',
    width: 180,
  },
  {
    title: '操作',
    slotName: 'operations',
    width: 150,
  },
]

// 获取角色颜色
const getRoleColor = (role: string) => {
  switch (role) {
    case 'admin':
      return 'red'
    case 'user':
      return 'blue'
    default:
      return 'gray'
  }
}

// 获取角色文本
const getRoleText = (role: string) => {
  switch (role) {
    case 'admin':
      return '管理员'
    case 'user':
      return '普通用户'
    default:
      return '未知'
  }
}

// 格式化日期
const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 加载用户列表
const loadUserList = async () => {
  try {
    loading.value = true
    const res = await getUserList({
      current: pagination.value.current,
      pageSize: pagination.value.pageSize,
      searchText: searchText.value,
    })

    if (res.code === 0) {
      userList.value = res.data.records
      pagination.value.total = res.data.total
    } else {
      Message.error('获取用户列表失败')
    }
  } catch (error) {
    Message.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.value.current = 1
  loadUserList()
}

// 重置搜索
const resetSearch = () => {
  searchText.value = ''
  pagination.value.current = 1
  loadUserList()
}

// 分页处理
const handlePageChange = (page: number) => {
  pagination.value.current = page
  loadUserList()
}

const handlePageSizeChange = (pageSize: number) => {
  pagination.value.pageSize = pageSize
  pagination.value.current = 1
  loadUserList()
}

// 显示添加用户模态框
const showAddUserModal = () => {
  isEdit.value = false
  userForm.value = {
    userAccount: '',
    userName: '',
    userPassword: '',
    userRole: 'user',
    userProfile: '',
  }
  userModalVisible.value = true
}

// 显示编辑用户模态框
const showEditUserModal = (user: UserVO) => {
  isEdit.value = true
  currentUser.value = user
  userForm.value = {
    userAccount: user.userAccount,
    userName: user.userName,
    userPassword: '',
    userRole: user.userRole,
    userProfile: user.userProfile || '',
  }
  userModalVisible.value = true
}

// 显示删除用户模态框
const showDeleteUserModal = (user: UserVO) => {
  currentUser.value = user
  deleteModalVisible.value = true
}

// 提交用户信息
const handleSubmitUser = async () => {
  try {
    if (isEdit.value) {
      // 编辑用户
      const updateRequest: UserUpdateRequest = {
        id: currentUser.value!.id,
        userAccount: userForm.value.userAccount,
        userName: userForm.value.userName,
        userRole: userForm.value.userRole,
        userProfile: userForm.value.userProfile,
      }

      // 如果密码不为空，则更新密码
      if (userForm.value.userPassword) {
        updateRequest.userPassword = userForm.value.userPassword
      }

      const res = await updateUser(updateRequest)
      if (res.code === 0) {
        Message.success('用户更新成功')
        userModalVisible.value = false
        loadUserList()
      } else {
        Message.error(res.message || '用户更新失败')
      }
    } else {
      // 添加用户
      const addRequest: UserAddRequest = {
        userAccount: userForm.value.userAccount,
        userName: userForm.value.userName,
        userPassword: userForm.value.userPassword,
        userRole: userForm.value.userRole,
        userProfile: userForm.value.userProfile,
      }

      const res = await addUser(addRequest)
      if (res.code === 0) {
        Message.success('用户添加成功')
        userModalVisible.value = false
        loadUserList()
      } else {
        Message.error(res.message || '用户添加失败')
      }
    }
  } catch (error) {
    Message.error('操作失败')
  }
}

// 取消用户操作
const handleCancelUser = () => {
  userModalVisible.value = false
  currentUser.value = null
}

// 删除用户
const handleDeleteUser = async () => {
  try {
    const res = await deleteUser({ id: currentUser.value!.id })
    if (res.code === 0) {
      Message.success('用户删除成功')
      deleteModalVisible.value = false
      loadUserList()
    } else {
      Message.error(res.message || '用户删除失败')
    }
  } catch (error) {
    Message.error('删除失败')
  } finally {
    currentUser.value = null
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadUserList()
})
</script>

<style scoped>
.manage-user-container {
  padding: 20px;
}

.user-card {
  min-height: 600px;
}

.search-area {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}
</style>
