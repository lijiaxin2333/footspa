//
//  CardTypeManagementView.swift
//  footspa
//
//  Created by ljx on 2025/11/13.
//

import SwiftUI

struct CardTypeManagementView: View {
    
    @State private var name: String = ""
    @ObservedObject private var db = FootspaDatabase.shared
    
    var body: some View {
        VStack(spacing: 20) {
                    HStack {
                        TextField("输入名字", text: $name)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                        Button("添加") {
                            addNode()
                        }
                    }
                    .padding()
                    
            List(db.moneyNodes, id: \.id) { node in
                        VStack(alignment: .leading) {
                            Text(node.name)
                                .font(.headline)
                            Text(node.type.rawValue)
                                .font(.subheadline)
                                .foregroundColor(.gray)
                        }
                    }
                }
                .navigationTitle("Money Nodes")
    }
    
    private func addNode() {
            guard !name.isEmpty else { return }
            do {
                let node = try buildMoneyNode { builder in
                    builder.name = name
                    builder.type = .customer
                    builder.cardId = 12123
                    builder.cardValid = true
                }
                try db.insertMoneyNode(node)
                name = "" // 清空输入框
            } catch {
                print("插入失败:", error)
            }
        }
}

#Preview {
    CardTypeManagementView()
}
