files = ls('../data_sensitivity/*distance-d30-u*-c0.200.txt');

figure();
hold on;
box on;
grid on;
styles = {'-.', '--', '-'};

auc = zeros(size(files, 1), 3);
for i = 1:size(files,1)
    dml5 = importdata(['../data_sensitivity/', files(i,:)]);
    [~, ind] = sort(dml5(:,6));
    dml5 = dml5(ind, :);


    dml5 = dml5(dml5(:,6) > 1,:);
    size(dml5)

    prod_colcEnt_cm = dml5(:,3);
    locen = dml5(:,4);
    pbg = dml5(:,5);
    freq = dml5(:,6);
    pbg_locen_td = dml5(:,7);
    td = dml5(:,8);
    frilabel = dml5(:,9);


    % Use the prec-recal function from Internet.
%     figure();
%     prec_rec( locwf5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'r--' );
%     hold on;
%     prec_rec( prod_colcEnt_cm, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'g-' );
%     prec_rec( locm5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'b:');
%     prec_rec( locf5, dl5, 'plotROC', 0, 'holdFigure', 1, 'style', 'c--');
    % My own precision-recall plot function    

    [pre, rec] = precisionRecallPlot( freq, frilabel, 'linestyle', styles{1}, 'color', [0+i * 0.1, 0.3, 0.3] );
    auc(i, 1) = trapz(rec, pre);
    
    [pre, rec] = precisionRecallPlot( locen, frilabel, 'linestyle', styles{2}, 'color', [0+i * 0.1, 0.3, 0.3] );
    auc(i, 2) = trapz(rec, pre);
 
    [pre, rec] = precisionRecallPlot( pbg_locen_td, frilabel, 'linestyle', styles{3}, 'color', [0+i * 0.1, 0.3, 0.3]);
    auc(i, 3) = trapz(rec, pre);

%     title(num2str(condition));
    
%     axis([0,1,0.5,1]);
end
    
%     precisionRecallPlot( locm5, dl5, 'r--' );
%     hline = findobj(gcf, 'type', 'line');
%     set(hline, 'linewidth', 3);
%     precisionRecallPlot( locf5, dl5, 'linestyle', ':', 'color', 'blue', 'linewidth', 5);
    xlabel('Recall', 'fontsize', 20);
    ylabel('Precision', 'fontsize', 20);
    set(gca, 'linewidth', 2, 'fontsize', 18);
    legend({'5000 users', '500 users','100 users', 'Frequency'}, 'location', 'best');
    %    'Location ID measure', 'Location ID frequency'}, 'fontsize', 16);
    set(gcf, 'PaperUnits', 'inches');
    print(['pr-tn.eps'], '-dpsc');
    system(['epstopdf pr-tn.eps']);
%     saveas(gcf, ['dist-wsum-d30-u5000fgt',num2str(condition),'.png']);
%     saveas(gcf, ['freq-wfbu5000fgt',num2str(condition),'.fig']);


frequency = auc(:,1);
locen = auc(:,2);
pbg_locen_td = auc(:,3);
x = [100, 200, 300, 400, 500, 1000, 2000, 3000, 4000, 5000];

figure;
hold on;
grid on;
box on;
plot(x, frequency, '-', 'color', [200, 0, 0] / 255, 'linewidth', 3);
plot(x, locen, '--', 'color', [0.3, 0.6, 0.9], 'linewidth', 3);
plot(x, pbg_locen_td, '-.', 'color', [0, 100, 0] / 255, 'linewidth', 3);
set(gca, 'linewidth', 2, 'fontsize', 18);
% axis([0,550, 0, 0.2]);
xlabel('Average #users', 'fontsize', 20);
ylabel('AUC', 'fontsize', 20);
legend({'Frequency', 'Global', 'Per+Glo+Tem'}, 'location', 'northeast');
set(gcf, 'paperunits', 'inches');
print('sensitivity-users.eps', '-dpsc');
system('epstopdf sensitivity-users.eps');